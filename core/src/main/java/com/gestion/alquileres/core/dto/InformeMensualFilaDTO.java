package com.gestion.alquileres.core.dto;

import java.math.BigDecimal;

/** Fila del Excel mensual (funcionalidad 5). */
public record InformeMensualFilaDTO(
        String direccionCompleta,
        String inquilino,
        String dni,
        BigDecimal rentaContrato,
        BigDecimal importeCobrado,
        String fechas,
        String origen) {
}
