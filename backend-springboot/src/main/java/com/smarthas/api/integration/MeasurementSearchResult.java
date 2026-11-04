package com.smarthas.api.integration;

import java.util.List;

/**
 * Resultado da busca dinamica: total sem o limite, comando gerado (no Oracle, o SQL montado
 * pela procedure; no H2, a descricao dos filtros aplicados em Java) e as linhas.
 */
public record MeasurementSearchResult(String engine, long total, String generatedQuery, List<MeasurementSearchRow> rows) { }
