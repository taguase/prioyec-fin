package com.gestion.alquileres.core.service;

import com.gestion.alquileres.core.dto.FilaInquilinoDTO;
import com.gestion.alquileres.core.dto.ResultadoCargaDTO;
import com.gestion.alquileres.core.model.*;
import com.gestion.alquileres.core.repository.*;
import com.gestion.alquileres.core.util.TextoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * FUNCIONALIDAD 1 - Carga del Excel de inquilinos.
 *
 * <p>Cada fila del fichero alimenta tres tablas: {@code inmueble} (direccion),
 * {@code inquilino} (persona) y {@code contrato} (fechas, renta y marca de
 * inquilino actual). Todo es idempotente: volver a cargar el mismo fichero
 * actualiza en lugar de duplicar.</p>
 */
@Service
public class CargaInquilinosService {

    private static final Logger log = LoggerFactory.getLogger(CargaInquilinosService.class);

    private final InmuebleRepository inmuebles;
    private final InquilinoRepository inquilinos;
    private final ContratoRepository contratos;
    private final CargaFicheroRepository cargas;

    public CargaInquilinosService(InmuebleRepository inmuebles, InquilinoRepository inquilinos,
                                  ContratoRepository contratos, CargaFicheroRepository cargas) {
        this.inmuebles = inmuebles;
        this.inquilinos = inquilinos;
        this.contratos = contratos;
        this.cargas = cargas;
    }

    @Transactional
    public ResultadoCargaDTO procesar(List<FilaInquilinoDTO> filas, String nombreFichero, String usuario) {
        ResultadoCargaDTO resultado = new ResultadoCargaDTO();
        resultado.setNombreFichero(nombreFichero);
        resultado.setLeidos(filas.size());

        CargaFichero carga = new CargaFichero();
        carga.setTipo(TipoCarga.INQUILINOS);
        carga.setNombreFichero(nombreFichero);
        carga.setUsuario(usuario);
        carga.setRegistrosLeidos(filas.size());
        carga = cargas.save(carga);

        for (FilaInquilinoDTO fila : filas) {
            if (!fila.isValida()) {
                resultado.setErroneos(resultado.getErroneos() + 1);
                resultado.addIncidencia("Fila " + fila.getFila() + ": " + fila.getError());
                continue;
            }
            try {
                procesarFila(fila, resultado);
                resultado.setCorrectos(resultado.getCorrectos() + 1);
            } catch (Exception e) {
                resultado.setErroneos(resultado.getErroneos() + 1);
                resultado.addIncidencia("Fila " + fila.getFila() + ": " + e.getMessage());
                log.warn("Error procesando la fila {} del fichero {}", fila.getFila(), nombreFichero, e);
            }
        }

        carga.setRegistrosOk(resultado.getCorrectos());
        carga.setRegistrosKo(resultado.getErroneos());
        carga.setObservaciones(String.join("\n", resultado.getIncidencias()));
        cargas.save(carga);
        resultado.setCargaId(carga.getId());
        return resultado;
    }

    private void procesarFila(FilaInquilinoDTO fila, ResultadoCargaDTO resultado) {
        Inmueble inmueble = obtenerOCrearInmueble(fila, resultado);
        Inquilino inquilino = obtenerOCrearInquilino(fila, resultado);

        Contrato contrato = contratos
                .findByInquilinoIdAndInmuebleIdAndFechaInicio(inquilino.getId(), inmueble.getId(), fila.getFechaInicioContrato())
                .orElseGet(Contrato::new);
        boolean nuevo = contrato.getId() == null;

        contrato.setInquilino(inquilino);
        contrato.setInmueble(inmueble);
        contrato.setFechaInicio(fila.getFechaInicioContrato());
        contrato.setFechaFin(fila.getFechaFinContrato());
        contrato.setImporteRenta(fila.getImporteRenta() == null ? BigDecimal.ZERO : fila.getImporteRenta());
        contrato.setActual(fila.isInquilinoActual());
        contrato = contratos.save(contrato);

        // Solo puede haber un contrato actual por inmueble: se desmarcan los demas.
        if (contrato.isActual()) {
            List<Contrato> otros = contratos.findByInmuebleIdAndActualTrueAndIdNot(inmueble.getId(), contrato.getId());
            for (Contrato otro : otros) {
                otro.setActual(false);
                contratos.save(otro);
                resultado.addIncidencia("Fila " + fila.getFila() + ": el contrato "
                        + otro.getId() + " de " + inmueble.getDireccionCompleta()
                        + " deja de ser el actual");
            }
            contratos.flush();
        }

        if (nuevo) resultado.setContratosNuevos(resultado.getContratosNuevos() + 1);
        else resultado.setContratosActualizados(resultado.getContratosActualizados() + 1);
    }

    private Inmueble obtenerOCrearInmueble(FilaInquilinoDTO f, ResultadoCargaDTO resultado) {
        String calle = limpiar(f.getNombreCalle());
        String portal = limpiar(f.getPortal());
        String piso = limpiar(f.getPiso());
        String letra = limpiar(f.getLetra()).toUpperCase();
        String municipio = limpiar(f.getMunicipio());
        String ciudad = limpiar(f.getCiudad());

        return inmuebles.findByNombreCalleAndPortalAndPisoAndLetraAndMunicipioAndCiudad(
                        calle, portal, piso, letra, municipio, ciudad)
                .orElseGet(() -> {
                    Inmueble nuevo = new Inmueble();
                    nuevo.setNombreCalle(calle);
                    nuevo.setPortal(portal);
                    nuevo.setPiso(piso);
                    nuevo.setLetra(letra);
                    nuevo.setMunicipio(municipio);
                    nuevo.setCiudad(ciudad);
                    resultado.setInmueblesNuevos(resultado.getInmueblesNuevos() + 1);
                    return inmuebles.save(nuevo);
                });
    }

    private Inquilino obtenerOCrearInquilino(FilaInquilinoDTO f, ResultadoCargaDTO resultado) {
        String dni = limpiar(f.getDni()).toUpperCase();
        Inquilino inquilino = inquilinos.findByDniIgnoreCase(dni).orElseGet(() -> {
            resultado.setInquilinosNuevos(resultado.getInquilinosNuevos() + 1);
            Inquilino nuevo = new Inquilino();
            nuevo.setDni(dni);
            return nuevo;
        });
        inquilino.setNombre(limpiar(f.getNombre()));
        inquilino.setApellidos(limpiar(f.getApellidos()));
        inquilino.setNombreBusqueda(TextoUtil.claveInquilino(inquilino.getNombre(), inquilino.getApellidos()));
        return inquilinos.save(inquilino);
    }

    private static String limpiar(String s) { return s == null ? "" : s.trim().replaceAll("\\s+", " "); }
}
