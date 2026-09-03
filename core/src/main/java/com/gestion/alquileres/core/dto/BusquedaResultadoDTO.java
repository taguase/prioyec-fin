package com.gestion.alquileres.core.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Fila de resultado de la busqueda de inquilinos (funcionalidad 2):
 * nombre, apellidos, DNI, direccion completa del inmueble y renta.
 */
public record BusquedaResultadoDTO(
        Long contratoId,
        Long inmuebleId,
        String nombre,
        String apellidos,
        String dni,
        String direccionCompleta,
        BigDecimal renta,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        boolean actual) {
}
