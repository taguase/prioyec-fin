package com.gestion.alquileres.core.model;

import jakarta.persistence.*;

/**
 * Persona que alquila un inmueble. El DNI es la clave funcional.
 *
 * <p>{@code nombreBusqueda} guarda "APELLIDOS NOMBRE" normalizado (mayusculas,
 * sin acentos, espacios colapsados) y es lo que se busca dentro del concepto
 * bancario del fichero mensual.</p>
 */
@Entity
@Table(name = "inquilino", uniqueConstraints = @UniqueConstraint(name = "uk_inquilino_dni", columnNames = "dni"))
public class Inquilino {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "apellidos", nullable = false, length = 200)
    private String apellidos;

    @Column(name = "dni", nullable = false, length = 20)
    private String dni;

    @Column(name = "nombre_busqueda", nullable = false, length = 320)
    private String nombreBusqueda;

    @Transient
    public String getNombreCompleto() {
        return (nombre == null ? "" : nombre) + " " + (apellidos == null ? "" : apellidos);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String v) { this.nombre = v; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String v) { this.apellidos = v; }
    public String getDni() { return dni; }
    public void setDni(String v) { this.dni = v; }
    public String getNombreBusqueda() { return nombreBusqueda; }
    public void setNombreBusqueda(String v) { this.nombreBusqueda = v; }
}
