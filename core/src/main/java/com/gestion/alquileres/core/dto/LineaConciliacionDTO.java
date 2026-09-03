package com.gestion.alquileres.core.dto;

import java.math.BigDecimal;

/**
 * Fila del informe de conciliacion mensual (funcionalidad 3): un inmueble con
 * su inquilino y, a continuacion, el importe y las fechas encontradas en el
 * fichero. Si se casaron varios movimientos, {@code importeEncontrado} es la
 * suma y {@code fechas} las concatena.
 */
public class LineaConciliacionDTO {

    private String direccionCompleta;
    private String nombre;
    private String apellidos;
    private String dni;
    private BigDecimal rentaContrato;
    private BigDecimal importeEncontrado;
    private String fechas;
    private int numMovimientos;

    public boolean isEncontrado() { return numMovimientos > 0; }

    public String getDireccionCompleta() { return direccionCompleta; }
    public void setDireccionCompleta(String v) { this.direccionCompleta = v; }
    public String getNombre() { return nombre; }
    public void setNombre(String v) { this.nombre = v; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String v) { this.apellidos = v; }
    public String getDni() { return dni; }
    public void setDni(String v) { this.dni = v; }
    public BigDecimal getRentaContrato() { return rentaContrato; }
    public void setRentaContrato(BigDecimal v) { this.rentaContrato = v; }
    public BigDecimal getImporteEncontrado() { return importeEncontrado; }
    public void setImporteEncontrado(BigDecimal v) { this.importeEncontrado = v; }
    public String getFechas() { return fechas; }
    public void setFechas(String v) { this.fechas = v; }
    public int getNumMovimientos() { return numMovimientos; }
    public void setNumMovimientos(int v) { this.numMovimientos = v; }
}
