package com.smarthas.api.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/** Limites da classificacao (mesma regra de PKG_SHAS_UTIL.FN_CLASSIFICAR). */
class ClassificationTest {

    @ParameterizedTest(name = "{0}x{1} -> {2}")
    @CsvSource({
            "119, 79, NORMAL",
            "120, 70, ELEVATED",
            "110, 80, ELEVATED",
            "139, 89, ELEVATED",
            "140, 70, HYPERTENSION",
            "130, 90, HYPERTENSION",
            "141, 85, HYPERTENSION",
            "150, 70, HYPERTENSION",
            "186, 121, HYPERTENSION"
    })
    void classificaPelosLimitesDaDiretriz(int systolic, int diastolic, Classification expected) {
        assertThat(Classification.of(systolic, diastolic)).isEqualTo(expected);
    }
}
