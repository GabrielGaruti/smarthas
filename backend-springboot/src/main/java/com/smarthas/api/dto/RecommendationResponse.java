package com.smarthas.api.dto;

import java.util.List;

/**
 * Saida da camada de apoio a decisao ("AI Logistics Extension"):
 * resume o quadro do paciente e gera recomendacoes automaticas.
 * Fase 6: inclui o indicador e o resumo calculados pelas functions PL/SQL.
 */
public record RecommendationResponse(
        int totalMeasurements,
        long normalCount,
        long elevatedCount,
        long hypertensionCount,
        String riskLevel,          // BAIXO, MODERADO, ALTO
        List<String> recommendations,
        HealthUnitResponse nearestUnit,
        Double controlRate30d,     // FN_SHAS_TAXA_CONTROLE
        String patientSummary,     // FN_SHAS_RESUMO_PACIENTE
        long openAlerts,
        String rulesEngine         // ORACLE_PLSQL ou JAVA_FALLBACK
) { }
