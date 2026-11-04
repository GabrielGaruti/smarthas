package com.smarthas.api.domain;

/**
 * Classificacao da pressao arterial.
 * Regra reaproveitada do app original (measurement.dart / MeasurementViewModel.kt),
 * mantida identica para que a migracao seja fiel.
 */
public enum Classification {
    NORMAL("Normal", "#4CAF50"),
    ELEVATED("Elevada", "#FF9800"),
    HYPERTENSION("Hipertensao", "#F44336");

    private final String label;
    private final String colorHex;

    Classification(String label, String colorHex) {
        this.label = label;
        this.colorHex = colorHex;
    }

    public String getLabel() { return label; }
    public String getColorHex() { return colorHex; }

    /**
     * Classifica uma medicao a partir dos valores sistolico/diastolico.
     * Fase 7: a faixa de hipertensao (>= 140 ou >= 90) passou a ser testada primeiro. Antes, 141x85
     * caia em "Elevada" porque a diastolica 80-89 era avaliada antes (bug apontado pelos testes).
     */
    public static Classification of(int systolic, int diastolic) {
        if (systolic >= 140 || diastolic >= 90) {
            return HYPERTENSION;
        } else if (systolic >= 120 || diastolic >= 80) {
            return ELEVATED;
        } else {
            return NORMAL;
        }
    }
}
