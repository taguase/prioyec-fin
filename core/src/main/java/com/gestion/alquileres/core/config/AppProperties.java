package com.gestion.alquileres.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Parametros funcionales de la aplicacion (prefijo {@code app} en application.yml). */
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Password password = new Password();
    private final Correo correo = new Correo();

    public Password getPassword() { return password; }
    public Correo getCorreo() { return correo; }

    public static class Password {
        /** Dias de validez de una contrasena nueva. */
        private int diasValidez = 90;
        /** Dias de antelacion con los que se avisa antes de caducar. */
        private int diasPreaviso = 7;

        public int getDiasValidez() { return diasValidez; }
        public void setDiasValidez(int v) { this.diasValidez = v; }
        public int getDiasPreaviso() { return diasPreaviso; }
        public void setDiasPreaviso(int v) { this.diasPreaviso = v; }
    }

    public static class Correo {
        /** Si es false, los avisos solo se escriben en el log (util en desarrollo). */
        private boolean habilitado = true;
        /** Remitente de los avisos. */
        private String remitente = "no-reply@gestion-alquileres.local";
        /** Copia opcional para el administrador. */
        private String copiaAdministrador;
        private String asuntoCaducada = "[Gestion de alquileres] Su contrasena ha caducado";
        private String asuntoPreaviso = "[Gestion de alquileres] Su contrasena esta a punto de caducar";

        public boolean isHabilitado() { return habilitado; }
        public void setHabilitado(boolean v) { this.habilitado = v; }
        public String getRemitente() { return remitente; }
        public void setRemitente(String v) { this.remitente = v; }
        public String getCopiaAdministrador() { return copiaAdministrador; }
        public void setCopiaAdministrador(String v) { this.copiaAdministrador = v; }
        public String getAsuntoCaducada() { return asuntoCaducada; }
        public void setAsuntoCaducada(String v) { this.asuntoCaducada = v; }
        public String getAsuntoPreaviso() { return asuntoPreaviso; }
        public void setAsuntoPreaviso(String v) { this.asuntoPreaviso = v; }
    }
}
