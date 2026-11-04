package com.smarthas.api.integration;

/** Linha da estatistica agrupada por dimensao (PKG_SHAS_INDICADORES.FN_ESTATISTICA_POR_DIMENSAO). */
public record DimensionStat(String group, long measurements, Double avgSystolic, Double avgDiastolic, Double controlRate) { }
