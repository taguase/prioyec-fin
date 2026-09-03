package com.gestion.alquileres.core.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Importe cobrado de un contrato en un mes concreto.
 *
 * <p>Solo existe un registro por contrato y periodo. Cuando el fichero mensual
 * aporta varios movimientos para el mismo inquilino, el importe se acumula y
 * todas las fechas se concatenan en {@code fechasPago}.</p>
 */
@Entity
@Table(name = "renta_mensual",
       uniqueConstraints = @UniqueConstraint(name = "uk_renta_contrato_periodo",
               columnNames = {"contrato_id", "anio", "mes"}))
public class RentaMensual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "contrato_id", nullable = false)
    private Contrato contrato;

    @Column(name = "anio", nullable = false)
    private int anio;

    @Column(name = "mes", nullable = false)
    private int mes;

    @Column(name = "importe", nullable = false, precision = 12, scale = 2)
    private BigDecimal importe = BigDecimal.ZERO;

    /** Primera fecha de cobro del periodo (la que se usa para ordenar). */
    @Column(name = "fecha_pago")
    private LocalDate fechaPago;

    /** Todas las fechas de cobro del periodo separadas por " | ". */
    @Column(name = "fechas_pago", length = 255)
    private String fechasPago;

    @Column(name = "num_movimientos", nullable = false)
    private int numMovimientos;

    @Enumerated(EnumType.STRING)
    @Column(name = "origen", nullable = false, length = 20)
    private OrigenRenta origen = OrigenRenta.MANUAL;

    @Column(name = "carga_id")
    private Long cargaId;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Contrato getContrato() { return contrato; }
    public void setContrato(Contrato v) { this.contrato = v; }
    public int getAnio() { return anio; }
    public void setAnio(int v) { this.anio = v; }
    public int getMes() { return mes; }
    public void setMes(int v) { this.mes = v; }
    public BigDecimal getImporte() { return importe; }
    public void setImporte(BigDecimal v) { this.importe = v; }
    public LocalDate getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDate v) { this.fechaPago = v; }
    public String getFechasPago() { return fechasPago; }
    public void setFechasPago(String v) { this.fechasPago = v; }
    public int getNumMovimientos() { return numMovimientos; }
    public void setNumMovimientos(int v) { this.numMovimientos = v; }
    public OrigenRenta getOrigen() { return origen; }
    public void setOrigen(OrigenRenta v) { this.origen = v; }
    public Long getCargaId() { return cargaId; }
    public void setCargaId(Long v) { this.cargaId = v; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime v) { this.fechaRegistro = v; }
}
