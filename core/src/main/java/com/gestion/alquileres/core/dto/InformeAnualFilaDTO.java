package com.gestion.alquileres.core.dto;

import java.math.BigDecimal;

/**
 * Fila del Excel anual (funcionalidad 6): un inmueble, sus inquilinos del anio
 * y los doce importes mensuales en columnas consecutivas.
 */
public class InformeAnualFilaDTO {

    private String direccionCompleta;
    private String inquilinos;
    private String dnis;
    private final BigDecimal[] importes = new BigDecimal[12];

    public InformeAnualFilaDTO() {
        for (int i = 0; i < 12; i++) importes[i] = BigDecimal.ZERO;
    }

    public void acumular(int mes, BigDecimal importe) {
        if (mes < 1 || mes > 12 || importe == null) return;
        importes[mes - 1] = importes[mes - 1].add(importe);
    }

    public BigDecimal getTotal() {
        BigDecimal t = BigDecimal.ZERO;
        for (BigDecimal i : importes) t = t.add(i);
        return t;
    }

    public String getDireccionCompleta() { return direccionCompleta; }
    public void setDireccionCompleta(String v) { this.direccionCompleta = v; }
    public String getInquilinos() { return inquilinos; }
    public void setInquilinos(String v) { this.inquilinos = v; }
    public String getDnis() { return dnis; }
    public void setDnis(String v) { this.dnis = v; }
    public BigDecimal[] getImportes() { return importes; }
}
