package com.gestion.alquileres.core.service;

import com.gestion.alquileres.core.dto.LineaConciliacionDTO;
import com.gestion.alquileres.core.dto.MovimientoDTO;
import com.gestion.alquileres.core.dto.ResultadoConciliacionDTO;
import com.gestion.alquileres.core.model.*;
import com.gestion.alquileres.core.repository.*;
import com.gestion.alquileres.core.util.TextoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * FUNCIONALIDAD 3 - Carga y conciliacion del fichero mensual.
 *
 * <p>El Excel de entrada trae en la segunda columna un texto libre (el concepto
 * del apunte bancario) donde hay que localizar el nombre del inquilino; en la
 * tercera el importe y en la cuarta la fecha.</p>
 *
 * <p>Para cada contrato se acumulan todos los movimientos casados: los importes
 * se suman y las fechas se concatenan. El resultado incluye ademas el bloque de
 * movimientos que no han casado con ningun inquilino, que se vuelca al final del
 * Excel de salida. Todo queda grabado en {@code carga_fichero},
 * {@code movimiento_fichero} y {@code renta_mensual}.</p>
 */
@Service
public class ConciliacionService {

    private static final Logger log = LoggerFactory.getLogger(ConciliacionService.class);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    /** Separador con el que se concatenan las fechas de varios movimientos. */
    public static final String SEPARADOR_FECHAS = " | ";

    private final ContratoRepository contratos;
    private final CargaFicheroRepository cargas;
    private final MovimientoFicheroRepository movimientos;
    private final RentaMensualRepository rentas;

    public ConciliacionService(ContratoRepository contratos, CargaFicheroRepository cargas,
                               MovimientoFicheroRepository movimientos, RentaMensualRepository rentas) {
        this.contratos = contratos;
        this.cargas = cargas;
        this.movimientos = movimientos;
        this.rentas = rentas;
    }

    /** Acumulador temporal de lo cobrado por un contrato durante la conciliacion. */
    private static final class Acumulado {
        BigDecimal importe = BigDecimal.ZERO;
        final List<LocalDate> fechas = new ArrayList<>();
        int movimientos;
    }

    @Transactional
    public ResultadoConciliacionDTO conciliar(List<MovimientoDTO> lineas, int anio, int mes,
                                              String nombreFichero, String usuario) {
        ResultadoConciliacionDTO resultado = new ResultadoConciliacionDTO();
        resultado.setNombreFichero(nombreFichero);
        resultado.setAnio(anio);
        resultado.setMes(mes);
        resultado.setMovimientosLeidos(lineas.size());

        CargaFichero carga = new CargaFichero();
        carga.setTipo(TipoCarga.MENSUAL);
        carga.setNombreFichero(nombreFichero);
        carga.setUsuario(usuario);
        carga.setAnio(anio);
        carga.setMes(mes);
        carga.setRegistrosLeidos(lineas.size());
        carga = cargas.save(carga);

        List<Contrato> todos = contratos.findAllConDetalle();
        Map<Long, Acumulado> acumuladoPorContrato = new HashMap<>();
        int casados = 0;

        for (MovimientoDTO linea : lineas) {
            String conceptoNormalizado = TextoUtil.normalizar(linea.getConcepto());
            List<Contrato> candidatos = localizarCandidatos(conceptoNormalizado, todos);

            MovimientoFichero mov = new MovimientoFichero();
            mov.setCargaId(carga.getId());
            mov.setFila(linea.getFila());
            mov.setConcepto(linea.getConcepto());
            mov.setImporte(linea.getImporte());
            mov.setFechaMovimiento(linea.getFecha());

            if (candidatos.isEmpty()) {
                mov.setEstado(EstadoMovimiento.NO_CASADO);
                mov.setMotivo("No se ha localizado ningun inquilino en el concepto");
                linea.setMotivo(mov.getMotivo());
                resultado.getNoEncontrados().add(linea);
            } else {
                Contrato elegido = elegirContrato(candidatos, linea.getFecha());
                if (elegido == null) {
                    mov.setEstado(EstadoMovimiento.AMBIGUO);
                    mov.setMotivo("El concepto casa con varios inquilinos distintos");
                    linea.setMotivo(mov.getMotivo());
                    resultado.getNoEncontrados().add(linea);
                } else {
                    mov.setEstado(EstadoMovimiento.CASADO);
                    mov.setContrato(elegido);
                    Acumulado acumulado = acumuladoPorContrato.computeIfAbsent(elegido.getId(), k -> new Acumulado());
                    acumulado.importe = acumulado.importe.add(
                            linea.getImporte() == null ? BigDecimal.ZERO : linea.getImporte());
                    if (linea.getFecha() != null) acumulado.fechas.add(linea.getFecha());
                    acumulado.movimientos++;
                    casados++;
                }
            }
            movimientos.save(mov);
        }

        // Volcado a renta_mensual: un unico registro por contrato y periodo.
        for (Map.Entry<Long, Acumulado> e : acumuladoPorContrato.entrySet()) {
            guardarRenta(e.getKey(), anio, mes, e.getValue(), carga.getId());
        }

        carga.setRegistrosOk(casados);
        carga.setRegistrosKo(lineas.size() - casados);
        cargas.save(carga);
        resultado.setCargaId(carga.getId());

        construirListado(resultado, todos, acumuladoPorContrato);
        log.info("Conciliacion {} ({}/{}): {} movimientos, {} casados, {} sin casar",
                nombreFichero, mes, anio, lineas.size(), casados, lineas.size() - casados);
        return resultado;
    }

    /**
     * Contratos cuyo inquilino aparece en el concepto. Primero se prueban las
     * variantes literales del nombre y, si ninguna encaja, que todos los tokens
     * significativos del nombre esten presentes en el texto.
     */
    private List<Contrato> localizarCandidatos(String conceptoNormalizado, List<Contrato> todos) {
        if (conceptoNormalizado.isBlank()) return List.of();
        List<Contrato> candidatos = new ArrayList<>();
        for (Contrato c : todos) {
            Inquilino q = c.getInquilino();
            boolean encaja = TextoUtil.variantesNombre(q.getNombre(), q.getApellidos()).stream()
                    .anyMatch(conceptoNormalizado::contains);
            if (!encaja) {
                List<String> tokens = TextoUtil.tokensSignificativos(
                        TextoUtil.claveInquilino(q.getNombre(), q.getApellidos()));
                encaja = !tokens.isEmpty() && tokens.stream().allMatch(conceptoNormalizado::contains);
            }
            if (encaja) candidatos.add(c);
        }
        return candidatos;
    }

    /**
     * De entre los candidatos elige el contrato al que imputar el cobro.
     * Si hay varios inquilinos distintos devuelve null (movimiento ambiguo).
     */
    private Contrato elegirContrato(List<Contrato> candidatos, LocalDate fecha) {
        Set<Long> inquilinosDistintos = new HashSet<>();
        for (Contrato c : candidatos) inquilinosDistintos.add(c.getInquilino().getId());
        if (inquilinosDistintos.size() > 1) return null;
        if (candidatos.size() == 1) return candidatos.get(0);

        // Mismo inquilino con varios contratos: el vigente en la fecha del apunte...
        if (fecha != null) {
            for (Contrato c : candidatos) {
                boolean empezado = !c.getFechaInicio().isAfter(fecha);
                boolean noTerminado = c.getFechaFin() == null || !c.getFechaFin().isBefore(fecha);
                if (empezado && noTerminado) return c;
            }
        }
        // ...o en su defecto el marcado como actual, o el mas reciente.
        return candidatos.stream()
                .filter(Contrato::isActual)
                .findFirst()
                .orElseGet(() -> candidatos.stream()
                        .max(Comparator.comparing(Contrato::getFechaInicio))
                        .orElse(null));
    }

    private void guardarRenta(Long contratoId, int anio, int mes, Acumulado acumulado, Long cargaId) {
        RentaMensual renta = rentas.findByContratoIdAndAnioAndMes(contratoId, anio, mes)
                .orElseGet(RentaMensual::new);
        if (renta.getContrato() == null) {
            renta.setContrato(contratos.getReferenceById(contratoId));
            renta.setAnio(anio);
            renta.setMes(mes);
        }
        acumulado.fechas.sort(Comparator.naturalOrder());
        renta.setImporte(acumulado.importe);
        renta.setFechaPago(acumulado.fechas.isEmpty() ? null : acumulado.fechas.get(0));
        renta.setFechasPago(formatearFechas(acumulado.fechas));
        renta.setNumMovimientos(acumulado.movimientos);
        renta.setOrigen(OrigenRenta.FICHERO);
        renta.setCargaId(cargaId);
        rentas.save(renta);
    }

    /** Listado completo de inmuebles e inquilinos con lo encontrado para cada uno. */
    private void construirListado(ResultadoConciliacionDTO resultado, List<Contrato> todos,
                                  Map<Long, Acumulado> acumulados) {
        List<Contrato> aListar = todos.stream()
                .filter(c -> c.isActual() || acumulados.containsKey(c.getId()))
                .sorted(Comparator
                        .comparing((Contrato c) -> c.getInmueble().getNombreCalle())
                        .thenComparing(c -> c.getInmueble().getPortal())
                        .thenComparing(c -> c.getInmueble().getPiso())
                        .thenComparing(c -> c.getInmueble().getLetra()))
                .toList();

        for (Contrato c : aListar) {
            Acumulado a = acumulados.get(c.getId());
            LineaConciliacionDTO linea = new LineaConciliacionDTO();
            linea.setDireccionCompleta(c.getInmueble().getDireccionCompleta());
            linea.setNombre(c.getInquilino().getNombre());
            linea.setApellidos(c.getInquilino().getApellidos());
            linea.setDni(c.getInquilino().getDni());
            linea.setRentaContrato(c.getImporteRenta());
            linea.setImporteEncontrado(a == null ? BigDecimal.ZERO : a.importe);
            linea.setFechas(a == null ? "" : formatearFechas(a.fechas));
            linea.setNumMovimientos(a == null ? 0 : a.movimientos);
            resultado.getLineas().add(linea);
        }
    }

    private static String formatearFechas(List<LocalDate> fechas) {
        if (fechas == null || fechas.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (LocalDate f : fechas) {
            if (sb.length() > 0) sb.append(SEPARADOR_FECHAS);
            sb.append(f.format(FECHA));
        }
        return TextoUtil.recortar(sb.toString(), 255);
    }
}
