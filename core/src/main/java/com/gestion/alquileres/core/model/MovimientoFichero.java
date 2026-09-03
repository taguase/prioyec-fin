package com.gestion.alquileres.core.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Linea leida del fichero mensual, con el resultado de su casado.
 * Las lineas {@link EstadoMovimiento#NO_CASADO} son las que se vuelcan en el
 * bloque final del Excel de conciliacion.
 */
@Entity
@Table(name = "movimiento_fichero")
public class MovimientoFichero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "carga_id", nullable = false)
    private Long cargaId;

    @Column(name = "fila")
    private Integer fila;

    @Column(name = "concepto", columnDefinition = "text")
    private String concepto;

    @Column(name = "importe", precision = 12, scale = 2)
    private BigDecimal importe;

    @Column(name = "fecha_movimiento")
    private LocalDate fechaMovimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_id")
    private Contrato contrato;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoMovimiento estado;

    @Column(name = "motivo", length = 200)
    private String motivo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCargaId() { return cargaId; }
    public void setCargaId(Long v) { this.cargaId = v; }
    public Integer getFila() { return fila; }
    public void setFila(Integer v) { this.fila = v; }
    public String getConcepto() { return concepto; }
    public void setConcepto(String v) { this.concepto = v; }
    public BigDecimal getImporte() { return importe; }
    public void setImporte(BigDecimal v) { this.importe = v; }
    public LocalDate getFechaMovimiento() { return fechaMovimiento; }
    public void setFechaMovimiento(LocalDate v) { this.fechaMovimiento = v; }
    public Contrato getContrato() { return contrato; }
    public void setContrato(Contrato v) { this.contrato = v; }
    public EstadoMovimiento getEstado() { return estado; }
    public void setEstado(EstadoMovimiento v) { this.estado = v; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String v) { this.motivo = v; }
}
