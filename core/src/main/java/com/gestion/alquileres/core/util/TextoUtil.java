package com.gestion.alquileres.core.util;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Normalizacion de texto para el casado de nombres contra el concepto bancario.
 *
 * <p>Normalizar = mayusculas, sin acentos, sin signos de puntuacion y con los
 * espacios colapsados. Asi "Peréz-Gómez, José Mª" y "PEREZ GOMEZ JOSE Ma"
 * acaban siendo comparables.</p>
 */
public final class TextoUtil {

    private TextoUtil() { }

    /** Palabras que no aportan al casado (particulas de apellidos y ruido bancario). */
    private static final Set<String> IRRELEVANTES = Set.of(
            "DE", "DEL", "LA", "LAS", "LOS", "Y", "DA", "DO", "VAN", "VON",
            "TRANSFERENCIA", "TRASPASO", "RECIBO", "INGRESO", "ALQUILER", "RENTA",
            "CONCEPTO", "ORDENANTE", "PAGO", "MENSUALIDAD", "BIZUM");

    public static String normalizar(String texto) {
        if (texto == null) return "";
        String sinAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return sinAcentos.toUpperCase()
                .replaceAll("[^A-Z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /** Clave de busqueda que se guarda en {@code inquilino.nombre_busqueda}. */
    public static String claveInquilino(String nombre, String apellidos) {
        return normalizar((apellidos == null ? "" : apellidos) + " " + (nombre == null ? "" : nombre));
    }

    /** Tokens significativos (&gt;= 3 letras y no irrelevantes) de un nombre normalizado. */
    public static List<String> tokensSignificativos(String textoNormalizado) {
        List<String> tokens = new ArrayList<>();
        for (String t : textoNormalizado.split(" ")) {
            if (t.length() >= 3 && !IRRELEVANTES.contains(t)) tokens.add(t);
        }
        return tokens;
    }

    /**
     * Variantes literales del nombre que se buscan dentro del concepto, de la
     * mas fiable a la menos: "APELLIDOS NOMBRE", "NOMBRE APELLIDOS" y
     * "NOMBRE PRIMER-APELLIDO".
     */
    public static Set<String> variantesNombre(String nombre, String apellidos) {
        String n = normalizar(nombre);
        String a = normalizar(apellidos);
        Set<String> variantes = new LinkedHashSet<>();
        if (!a.isBlank() && !n.isBlank()) {
            variantes.add(a + " " + n);
            variantes.add(n + " " + a);
            String primerApellido = a.split(" ")[0];
            String primerNombre = n.split(" ")[0];
            variantes.add(primerNombre + " " + primerApellido);
            variantes.add(primerApellido + " " + primerNombre);
        }
        variantes.removeIf(String::isBlank);
        return variantes;
    }

    /** Recorta un texto largo para trazas y celdas de Excel. */
    public static String recortar(String texto, int max) {
        if (texto == null) return "";
        return texto.length() <= max ? texto : texto.substring(0, max - 3) + "...";
    }
}
