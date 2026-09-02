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

    /** Classifica uma medicao a partir dos valores sistolico/diastolico. */
    public static Classification of(int systolic, int diastolic) {
        if (systolic < 120 && diastolic < 80) {
            return NORMAL;
        } else if ((systolic >= 120 && systolic <= 139) || (diastolic >= 80 && diastolic <= 89)) {
            return ELEVATED;
        } else {
            return HYPERTENSION;
        }
    }
}
