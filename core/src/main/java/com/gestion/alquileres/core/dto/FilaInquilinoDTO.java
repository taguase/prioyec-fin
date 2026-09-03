package com.gestion.alquileres.core.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Una fila del Excel de carga de inquilinos (funcionalidad 1).
 * El modulo {@code excel} lee y valida; el modulo {@code core} persiste.
 */
public class FilaInquilinoDTO {

    private int fila;
    private String nombre;
    private String apellidos;
    private String dni;
    private LocalDate fechaInicioContrato;
    private LocalDate fechaFinContrato;
    private String nombreCalle;
    private String portal;
    private String piso;
    private String letra;
    private String municipio;
    private String ciudad;
    private BigDecimal importeRenta;
    private boolean inquilinoActual;
    /** Motivo por el que la fila no es procesable; null si es valida. */
    private String error;

    public boolean isValida() { return error == null; }

    public int getFila() { return fila; }
    public void setFila(int v) { this.fila = v; }
    public String getNombre() { return nombre; }
    public void setNombre(String v) { this.nombre = v; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String v) { this.apellidos = v; }
    public String getDni() { return dni; }
    public void setDni(String v) { this.dni = v; }
    public LocalDate getFechaInicioContrato() { return fechaInicioContrato; }
    public void setFechaInicioContrato(LocalDate v) { this.fechaInicioContrato = v; }
    public LocalDate getFechaFinContrato() { return fechaFinContrato; }
    public void setFechaFinContrato(LocalDate v) { this.fechaFinContrato = v; }
    public String getNombreCalle() { return nombreCalle; }
    public void setNombreCalle(String v) { this.nombreCalle = v; }
    public String getPortal() { return portal; }
    public void setPortal(String v) { this.portal = v; }
    public String getPiso() { return piso; }
    public void setPiso(String v) { this.piso = v; }
    public String getLetra() { return letra; }
    public void setLetra(String v) { this.letra = v; }
    public String getMunicipio() { return municipio; }
    public void setMunicipio(String v) { this.municipio = v; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String v) { this.ciudad = v; }
    public BigDecimal getImporteRenta() { return importeRenta; }
    public void setImporteRenta(BigDecimal v) { this.importeRenta = v; }
    public boolean isInquilinoActual() { return inquilinoActual; }
    public void setInquilinoActual(boolean v) { this.inquilinoActual = v; }
    public String getError() { return error; }
    public void setError(String v) { this.error = v; }
}
