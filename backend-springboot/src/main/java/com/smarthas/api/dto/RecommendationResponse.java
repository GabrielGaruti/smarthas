package com.smarthas.api.dto;

import java.util.List;

/**
 * Saida da camada de apoio a decisao ("AI Logistics Extension"):
 * resume o quadro do paciente e gera recomendacoes automaticas.
 */
public record RecommendationResponse(
        int totalMeasurements,
        long normalCount,
        long elevatedCount,
        long hypertensionCount,
        String riskLevel,          // BAIXO, MODERADO, ALTO
        List<String> recommendations,
        HealthUnitResponse nearestUnit
) { }
