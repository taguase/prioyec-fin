package com.gestion.alquileres.core.service;

import com.gestion.alquileres.core.dto.InformeAnualFilaDTO;
import com.gestion.alquileres.core.dto.InformeMensualFilaDTO;
import com.gestion.alquileres.core.model.Contrato;
import com.gestion.alquileres.core.model.RentaMensual;
import com.gestion.alquileres.core.repository.ContratoRepository;
import com.gestion.alquileres.core.repository.RentaMensualRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * FUNCIONALIDADES 5 y 6 - Datos de los informes mensual y anual.
 *
 * <p>Este servicio solo prepara los datos; el modulo {@code excel} se encarga de
 * volcarlos a un libro de Apache POI.</p>
 */
@Service
public class InformeService {

    private final ContratoRepository contratos;
    private final RentaMensualRepository rentas;

    public InformeService(ContratoRepository contratos, RentaMensualRepository rentas) {
        this.contratos = contratos;
        this.rentas = rentas;
    }

    /**
     * FUNCIONALIDAD 5 - Una fila por inmueble con su inquilino, el importe
     * cobrado en el mes y la fecha en la que se hizo. Se incluyen tambien los
     * contratos sin cobro registrado, con importe cero.
     */
    @Transactional(readOnly = true)
    public List<InformeMensualFilaDTO> datosMensuales(int anio, int mes) {
        Map<Long, RentaMensual> porContrato = new HashMap<>();
        for (RentaMensual r : rentas.findDelPeriodo(anio, mes)) {
            porContrato.put(r.getContrato().getId(), r);
        }

        List<Contrato> aListar = new ArrayList<>(contratos.findActualesOrdenadosPorDireccion());
        Set<Long> yaIncluidos = new HashSet<>();
        aListar.forEach(c -> yaIncluidos.add(c.getId()));
        // Contratos historicos que si tuvieron cobro en el periodo.
        for (RentaMensual r : porContrato.values()) {
            if (yaIncluidos.add(r.getContrato().getId())) aListar.add(r.getContrato());
        }
        aListar.sort(comparadorPorDireccion());

        List<InformeMensualFilaDTO> filas = new ArrayList<>(aListar.size());
        for (Contrato c : aListar) {
            RentaMensual r = porContrato.get(c.getId());
            filas.add(new InformeMensualFilaDTO(
                    c.getInmueble().getDireccionCompleta(),
                    c.getInquilino().getNombreCompleto(),
                    c.getInquilino().getDni(),
                    c.getImporteRenta(),
                    r == null ? BigDecimal.ZERO : r.getImporte(),
                    r == null ? "" : nz(r.getFechasPago()),
                    r == null ? "" : r.getOrigen().name()));
        }
        return filas;
    }

    /**
     * FUNCIONALIDAD 6 - Una fila por inmueble con sus inquilinos del anio y los
     * doce importes mensuales en columnas consecutivas.
     */
    @Transactional(readOnly = true)
    public List<InformeAnualFilaDTO> datosAnuales(int anio) {
        // Mapa inmueble -> acumulado anual, sembrado con los contratos vigentes
        // para que aparezcan tambien los inmuebles sin ningun cobro.
        Map<Long, InformeAnualFilaDTO> porInmueble = new LinkedHashMap<>();
        Map<Long, Set<String>> inquilinosPorInmueble = new LinkedHashMap<>();
        Map<Long, Set<String>> dnisPorInmueble = new LinkedHashMap<>();
        Map<Long, Contrato> muestraInmueble = new LinkedHashMap<>();

        for (Contrato c : contratos.findActualesOrdenadosPorDireccion()) {
            registrarInmueble(porInmueble, inquilinosPorInmueble, dnisPorInmueble, muestraInmueble, c);
        }
        for (RentaMensual r : rentas.findDelAnio(anio)) {
            Contrato c = r.getContrato();
            registrarInmueble(porInmueble, inquilinosPorInmueble, dnisPorInmueble, muestraInmueble, c);
            porInmueble.get(c.getInmueble().getId()).acumular(r.getMes(), r.getImporte());
        }

        List<Long> ordenados = new ArrayList<>(porInmueble.keySet());
        ordenados.sort(Comparator.comparing(id -> claveOrden(muestraInmueble.get(id))));

        List<InformeAnualFilaDTO> filas = new ArrayList<>(ordenados.size());
        for (Long id : ordenados) {
            InformeAnualFilaDTO fila = porInmueble.get(id);
            fila.setInquilinos(String.join(", ", inquilinosPorInmueble.get(id)));
            fila.setDnis(String.join(", ", dnisPorInmueble.get(id)));
            filas.add(fila);
        }
        return filas;
    }

    private void registrarInmueble(Map<Long, InformeAnualFilaDTO> porInmueble,
                                   Map<Long, Set<String>> inquilinos,
                                   Map<Long, Set<String>> dnis,
                                   Map<Long, Contrato> muestra,
                                   Contrato c) {
        Long id = c.getInmueble().getId();
        porInmueble.computeIfAbsent(id, k -> {
            InformeAnualFilaDTO f = new InformeAnualFilaDTO();
            f.setDireccionCompleta(c.getInmueble().getDireccionCompleta());
            return f;
        });
        inquilinos.computeIfAbsent(id, k -> new LinkedHashSet<>()).add(c.getInquilino().getNombreCompleto());
        dnis.computeIfAbsent(id, k -> new LinkedHashSet<>()).add(c.getInquilino().getDni());
        muestra.putIfAbsent(id, c);
    }

    private static Comparator<Contrato> comparadorPorDireccion() {
        return Comparator.comparing((Contrato c) -> c.getInmueble().getNombreCalle())
                .thenComparing(c -> c.getInmueble().getPortal())
                .thenComparing(c -> c.getInmueble().getPiso())
                .thenComparing(c -> c.getInmueble().getLetra());
    }

    private static String claveOrden(Contrato c) {
        if (c == null) return "";
        return String.join("|", c.getInmueble().getNombreCalle(), c.getInmueble().getPortal(),
                c.getInmueble().getPiso(), c.getInmueble().getLetra());
    }

    private static String nz(String s) { return s == null ? "" : s; }
}
