package com.gestion.alquileres.core.service;

import com.gestion.alquileres.core.dto.BusquedaResultadoDTO;
import com.gestion.alquileres.core.dto.EdificioDTO;
import com.gestion.alquileres.core.model.Contrato;
import com.gestion.alquileres.core.model.Inmueble;
import com.gestion.alquileres.core.repository.ContratoRepository;
import com.gestion.alquileres.core.repository.InmuebleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * FUNCIONALIDAD 2 - Busqueda de inquilinos.
 *
 * <p>Dos modos:</p>
 * <ul>
 *   <li><b>Por domicilio</b>: se elige el portal en un combo y despues el
 *       piso/letra en otro; se muestra el inquilino actual y, bajo demanda, el
 *       historico completo o solo los inquilinos anteriores.</li>
 *   <li><b>Por inquilino</b>: busqueda LIKE sobre nombre, apellidos o DNI.</li>
 * </ul>
 * En ambos casos las columnas mostradas son nombre, apellidos, DNI, direccion
 * completa del inmueble y renta.
 */
@Service
public class BusquedaService {

    private final InmuebleRepository inmuebles;
    private final ContratoRepository contratos;

    public BusquedaService(InmuebleRepository inmuebles, ContratoRepository contratos) {
        this.inmuebles = inmuebles;
        this.contratos = contratos;
    }

    /** Combo 1: direcciones (a nivel de portal) de los inmuebles dados de alta. */
    @Transactional(readOnly = true)
    public List<EdificioDTO> listarEdificios() {
        return inmuebles.findEdificios();
    }

    /** Combo 2: viviendas (piso y letra) del portal seleccionado. */
    @Transactional(readOnly = true)
    public List<Inmueble> listarViviendas(String claveEdificio) {
        if (claveEdificio == null || claveEdificio.isBlank()) return List.of();
        EdificioDTO e = EdificioDTO.deClave(claveEdificio);
        return inmuebles.findViviendasDeEdificio(e.nombreCalle(), e.portal(), e.municipio(), e.ciudad());
    }

    @Transactional(readOnly = true)
    public Optional<Inmueble> buscarInmueble(Long inmuebleId) {
        return inmuebles.findById(inmuebleId);
    }

    /** Inquilino actual de una vivienda. */
    @Transactional(readOnly = true)
    public Optional<BusquedaResultadoDTO> inquilinoActual(Long inmuebleId) {
        return contratos.findByInmuebleIdAndActualTrue(inmuebleId).map(BusquedaService::aDto);
    }

    /** Historico completo de la vivienda (actual + anteriores). */
    @Transactional(readOnly = true)
    public List<BusquedaResultadoDTO> historico(Long inmuebleId) {
        return contratos.findHistoricoDeInmueble(inmuebleId).stream().map(BusquedaService::aDto).toList();
    }

    /** Solo los inquilinos anteriores de la vivienda. */
    @Transactional(readOnly = true)
    public List<BusquedaResultadoDTO> anteriores(Long inmuebleId) {
        return contratos.findAnterioresDeInmueble(inmuebleId).stream().map(BusquedaService::aDto).toList();
    }

    /** Busqueda LIKE por nombre, apellidos o DNI del inquilino. */
    @Transactional(readOnly = true)
    public List<BusquedaResultadoDTO> buscarPorInquilino(String texto) {
        if (texto == null || texto.isBlank()) return List.of();
        String patron = "%" + texto.trim().toUpperCase() + "%";
        return contratos.buscarPorNombreODni(patron).stream().map(BusquedaService::aDto).toList();
    }

    static BusquedaResultadoDTO aDto(Contrato c) {
        return new BusquedaResultadoDTO(
                c.getId(),
                c.getInmueble().getId(),
                c.getInquilino().getNombre(),
                c.getInquilino().getApellidos(),
                c.getInquilino().getDni(),
                c.getInmueble().getDireccionCompleta(),
                c.getImporteRenta(),
                c.getFechaInicio(),
                c.getFechaFin(),
                c.isActual());
    }
}
