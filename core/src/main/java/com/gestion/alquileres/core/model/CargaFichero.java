package com.gestion.alquileres.core.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Traza de un fichero Excel procesado: quien, cuando, cuantas lineas y con que resultado. */
@Entity
@Table(name = "carga_fichero")
public class CargaFichero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoCarga tipo;

    @Column(name = "nombre_fichero", nullable = false, length = 255)
    private String nombreFichero;

    @Column(name = "fecha_carga", nullable = false)
    private LocalDateTime fechaCarga = LocalDateTime.now();

    @Column(name = "usuario", length = 50)
    private String usuario;

    @Column(name = "anio")
    private Integer anio;

    @Column(name = "mes")
    private Integer mes;

    @Column(name = "registros_leidos", nullable = false)
    private int registrosLeidos;

    @Column(name = "registros_ok", nullable = false)
    private int registrosOk;

    @Column(name = "registros_ko", nullable = false)
    private int registrosKo;

    @Column(name = "observaciones", columnDefinition = "text")
    private String observaciones;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TipoCarga getTipo() { return tipo; }
    public void setTipo(TipoCarga v) { this.tipo = v; }
    public String getNombreFichero() { return nombreFichero; }
    public void setNombreFichero(String v) { this.nombreFichero = v; }
    public LocalDateTime getFechaCarga() { return fechaCarga; }
    public void setFechaCarga(LocalDateTime v) { this.fechaCarga = v; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String v) { this.usuario = v; }
    public Integer getAnio() { return anio; }
    public void setAnio(Integer v) { this.anio = v; }
    public Integer getMes() { return mes; }
    public void setMes(Integer v) { this.mes = v; }
    public int getRegistrosLeidos() { return registrosLeidos; }
    public void setRegistrosLeidos(int v) { this.registrosLeidos = v; }
    public int getRegistrosOk() { return registrosOk; }
    public void setRegistrosOk(int v) { this.registrosOk = v; }
    public int getRegistrosKo() { return registrosKo; }
    public void setRegistrosKo(int v) { this.registrosKo = v; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String v) { this.observaciones = v; }
}
