package com.gestion.alquileres.core.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Una linea del Excel mensual (funcionalidad 3):
 * columna 2 = concepto donde buscar el inquilino, columna 3 = importe, columna 4 = fecha.
 */
public class MovimientoDTO {

    private int fila;
    private String concepto;
    private BigDecimal importe;
    private LocalDate fecha;
    private String motivo;

    public MovimientoDTO() { }

    public MovimientoDTO(int fila, String concepto, BigDecimal importe, LocalDate fecha) {
        this.fila = fila;
        this.concepto = concepto;
        this.importe = importe;
        this.fecha = fecha;
    }

    public int getFila() { return fila; }
    public void setFila(int v) { this.fila = v; }
    public String getConcepto() { return concepto; }
    public void setConcepto(String v) { this.concepto = v; }
    public BigDecimal getImporte() { return importe; }
    public void setImporte(BigDecimal v) { this.importe = v; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate v) { this.fecha = v; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String v) { this.motivo = v; }
}
