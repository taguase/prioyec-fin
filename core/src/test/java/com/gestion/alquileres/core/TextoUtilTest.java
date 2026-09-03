package com.gestion.alquileres.core;

import com.gestion.alquileres.core.util.TextoUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/** El casado del fichero mensual depende por completo de esta normalizacion. */
class TextoUtilTest {

    @Test
    @DisplayName("normalizar quita acentos, signos y espacios sobrantes")
    void normalizar() {
        assertThat(TextoUtil.normalizar("  Peréz-Gómez,  José Mª ")).isEqualTo("PEREZ GOMEZ JOSE M");
        assertThat(TextoUtil.normalizar(null)).isEmpty();
    }

    @Test
    @DisplayName("las variantes del nombre cubren los ordenes habituales del concepto bancario")
    void variantes() {
        Set<String> variantes = TextoUtil.variantesNombre("Maria", "Lopez Garcia");
        assertThat(variantes).contains("LOPEZ GARCIA MARIA", "MARIA LOPEZ GARCIA", "MARIA LOPEZ");
    }

    @Test
    @DisplayName("una variante del nombre aparece dentro del concepto de la transferencia")
    void seLocalizaDentroDelConcepto() {
        String concepto = TextoUtil.normalizar("TRANSFERENCIA DE MARIA LOPEZ GARCIA CONCEPTO ALQUILER ENERO");
        assertThat(TextoUtil.variantesNombre("Maria", "Lopez Garcia"))
                .anyMatch(concepto::contains);
    }

    @Test
    @DisplayName("los tokens significativos descartan particulas y ruido bancario")
    void tokens() {
        assertThat(TextoUtil.tokensSignificativos(TextoUtil.claveInquilino("Ana", "de la Torre Ruiz")))
                .containsExactlyInAnyOrder("TORRE", "RUIZ", "ANA");
    }
}
