package com.gestion.alquileres.excel;

/** Nombres de los meses en castellano, para titulos y cabeceras de informes. */
public final class NombresMes {

    private NombresMes() { }

    public static final String[] NOMBRES = {
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    public static String de(int mes) {
        return (mes >= 1 && mes <= 12) ? NOMBRES[mes - 1] : String.valueOf(mes);
    }
}
