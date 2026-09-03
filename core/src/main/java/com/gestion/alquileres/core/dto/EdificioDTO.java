package com.gestion.alquileres.core.dto;

/**
 * Direccion a nivel de portal (sin piso ni letra). Alimenta el primer combo de
 * la pantalla de busqueda por domicilio.
 */
public record EdificioDTO(String nombreCalle, String portal, String municipio, String ciudad) {

    /** Clave estable que viaja en el {@code value} del combo. */
    public String clave() {
        return String.join("|", nz(nombreCalle), nz(portal), nz(municipio), nz(ciudad));
    }

    public static EdificioDTO deClave(String clave) {
        String[] p = (clave == null ? "" : clave).split("\\|", -1);
        return new EdificioDTO(v(p, 0), v(p, 1), v(p, 2), v(p, 3));
    }

    /** Texto que ve el usuario en el desplegable. */
    public String descripcion() {
        StringBuilder sb = new StringBuilder(nz(nombreCalle));
        if (!nz(portal).isBlank())    sb.append(", portal ").append(portal);
        if (!nz(municipio).isBlank()) sb.append(", ").append(municipio);
        if (!nz(ciudad).isBlank() && !ciudad.equalsIgnoreCase(municipio)) sb.append(" (").append(ciudad).append(")");
        return sb.toString();
    }

    private static String nz(String s) { return s == null ? "" : s; }
    private static String v(String[] a, int i) { return i < a.length ? a[i] : ""; }
}
