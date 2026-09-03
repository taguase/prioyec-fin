package com.gestion.alquileres.core.model;

import jakarta.persistence.*;

/**
 * Vivienda en alquiler. La direccion completa (calle + portal + piso + letra +
 * municipio + ciudad) identifica de forma unica al inmueble.
 */
@Entity
@Table(name = "inmueble",
       uniqueConstraints = @UniqueConstraint(name = "uk_inmueble_direccion",
               columnNames = {"nombre_calle", "portal", "piso", "letra", "municipio", "ciudad"}))
public class Inmueble {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_calle", nullable = false, length = 200)
    private String nombreCalle;

    @Column(name = "portal", nullable = false, length = 20)
    private String portal = "";

    @Column(name = "piso", nullable = false, length = 20)
    private String piso = "";

    @Column(name = "letra", nullable = false, length = 10)
    private String letra = "";

    @Column(name = "municipio", nullable = false, length = 120)
    private String municipio = "";

    @Column(name = "ciudad", nullable = false, length = 120)
    private String ciudad = "";

    /** Direccion en una sola linea, tal y como se muestra en pantallas e informes. */
    @Transient
    public String getDireccionCompleta() {
        StringBuilder sb = new StringBuilder(nombreCalle == null ? "" : nombreCalle);
        if (tiene(portal))    sb.append(", portal ").append(portal);
        if (tiene(piso))      sb.append(", ").append(piso);
        if (tiene(letra))     sb.append(" ").append(letra);
        if (tiene(municipio)) sb.append(", ").append(municipio);
        if (tiene(ciudad) && !ciudad.equalsIgnoreCase(municipio)) sb.append(" (").append(ciudad).append(")");
        return sb.toString();
    }

    /** Direccion del portal, sin piso ni letra: es la que alimenta el combo de busqueda. */
    @Transient
    public String getDireccionPortal() {
        StringBuilder sb = new StringBuilder(nombreCalle == null ? "" : nombreCalle);
        if (tiene(portal))    sb.append(", portal ").append(portal);
        if (tiene(municipio)) sb.append(", ").append(municipio);
        return sb.toString();
    }

    private static boolean tiene(String s) { return s != null && !s.isBlank(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
}
