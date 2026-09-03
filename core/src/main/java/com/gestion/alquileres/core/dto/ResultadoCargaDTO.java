package com.gestion.alquileres.core.dto;

import java.util.ArrayList;
import java.util.List;

/** Resumen de una carga de fichero para mostrar en pantalla. */
public class ResultadoCargaDTO {

    private Long cargaId;
    private String nombreFichero;
    private int leidos;
    private int correctos;
    private int erroneos;
    private int inmueblesNuevos;
    private int inquilinosNuevos;
    private int contratosNuevos;
    private int contratosActualizados;
    private final List<String> incidencias = new ArrayList<>();

    public void addIncidencia(String s) { incidencias.add(s); }

    public Long getCargaId() { return cargaId; }
    public void setCargaId(Long v) { this.cargaId = v; }
    public String getNombreFichero() { return nombreFichero; }
    public void setNombreFichero(String v) { this.nombreFichero = v; }
    public int getLeidos() { return leidos; }
    public void setLeidos(int v) { this.leidos = v; }
    public int getCorrectos() { return correctos; }
    public void setCorrectos(int v) { this.correctos = v; }
    public int getErroneos() { return erroneos; }
    public void setErroneos(int v) { this.erroneos = v; }
    public int getInmueblesNuevos() { return inmueblesNuevos; }
    public void setInmueblesNuevos(int v) { this.inmueblesNuevos = v; }
    public int getInquilinosNuevos() { return inquilinosNuevos; }
    public void setInquilinosNuevos(int v) { this.inquilinosNuevos = v; }
    public int getContratosNuevos() { return contratosNuevos; }
    public void setContratosNuevos(int v) { this.contratosNuevos = v; }
    public int getContratosActualizados() { return contratosActualizados; }
    public void setContratosActualizados(int v) { this.contratosActualizados = v; }
    public List<String> getIncidencias() { return incidencias; }
}
