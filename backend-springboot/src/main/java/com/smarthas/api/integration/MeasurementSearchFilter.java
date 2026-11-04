package com.smarthas.api.integration;

/**
 * Filtros da busca dinamica de medicoes (PKG_SHAS_INDICADORES.PRC_CONSULTAR_MEDICOES).
 * Campos nulos sao ignorados; orderBy/direction aceitam apenas os valores da whitelist.
 */
public record MeasurementSearchFilter(
        Long userId,
        String classification,   // NORMAL | ELEVATED | HYPERTENSION
        String source,           // MANUAL | SENSOR
        Integer days,
        Integer minSystolic,
        String orderBy,          // DATA | SISTOLICA | DIASTOLICA | PACIENTE
        String direction,        // ASC | DESC
        int limit
) { }
