package com.smarthas.api.integration;

/** Resultado da avaliacao de uma leitura (parametros OUT da procedure). */
public record AlertOutcome(Long alertId, String severity) {
    public static AlertOutcome none() {
        return new AlertOutcome(null, "NENHUM");
    }

    public boolean generated() {
        return alertId != null;
    }
}
