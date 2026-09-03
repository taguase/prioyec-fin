package com.gestion.alquileres.core.model;

/** Resultado del casado de una linea del fichero mensual contra los contratos. */
public enum EstadoMovimiento {
    /** Se ha localizado exactamente un inquilino dentro del concepto. */
    CASADO,
    /** No se ha localizado ningun inquilino: va al bloque final del informe. */
    NO_CASADO,
    /** El concepto contiene varios inquilinos distintos: requiere revision manual. */
    AMBIGUO
}
