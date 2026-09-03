package com.gestion.alquileres.core.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Resultado completo de procesar el fichero mensual: el listado de todos los
 * inmuebles/inquilinos con lo cobrado, y detras el bloque de movimientos del
 * fichero que no han casado con ningun inquilino.
 */
public class ResultadoConciliacionDTO {

    private Long cargaId;
    private String nombreFichero;
    private int anio;
    private int mes;
    private int movimientosLeidos;
    private final List<LineaConciliacionDTO> lineas = new ArrayList<>();
    private final List<MovimientoDTO> noEncontrados = new ArrayList<>();

    public int getContratosConCobro() {
        return (int) lineas.stream().filter(LineaConciliacionDTO::isEncontrado).count();
    }

    public int getContratosSinCobro() { return lineas.size() - getContratosConCobro(); }

    public Long getCargaId() { return cargaId; }
    public void setCargaId(Long v) { this.cargaId = v; }
    public String getNombreFichero() { return nombreFichero; }
    public void setNombreFichero(String v) { this.nombreFichero = v; }
    public int getAnio() { return anio; }
    public void setAnio(int v) { this.anio = v; }
    public int getMes() { return mes; }
    public void setMes(int v) { this.mes = v; }
    public int getMovimientosLeidos() { return movimientosLeidos; }
    public void setMovimientosLeidos(int v) { this.movimientosLeidos = v; }
    public List<LineaConciliacionDTO> getLineas() { return lineas; }
    public List<MovimientoDTO> getNoEncontrados() { return noEncontrados; }
}
