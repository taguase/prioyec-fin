package com.gestion.alquileres.core.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Vinculo entre un inquilino y un inmueble durante un periodo, con su renta.
 *
 * <p>El flag {@code actual} identifica al inquilino vigente del inmueble; un
 * indice unico parcial en base de datos garantiza como maximo un contrato
 * actual por inmueble. El resto de contratos del inmueble forman su historico.</p>
 */
@Entity
@Table(name = "contrato")
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "inquilino_id", nullable = false)
    private Inquilino inquilino;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "inmueble_id", nullable = false)
    private Inmueble inmueble;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "importe_renta", nullable = false, precision = 12, scale = 2)
    private BigDecimal importeRenta = BigDecimal.ZERO;

    @Column(name = "actual", nullable = false)
    private boolean actual = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Inquilino getInquilino() { return inquilino; }
    public void setInquilino(Inquilino v) { this.inquilino = v; }
    public Inmueble getInmueble() { return inmueble; }
    public void setInmueble(Inmueble v) { this.inmueble = v; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate v) { this.fechaInicio = v; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate v) { this.fechaFin = v; }
    public BigDecimal getImporteRenta() { return importeRenta; }
    public void setImporteRenta(BigDecimal v) { this.importeRenta = v; }
    public boolean isActual() { return actual; }
    public void setActual(boolean v) { this.actual = v; }
}
