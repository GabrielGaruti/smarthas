package com.smarthas.api.dto;

import java.util.List;

/** Resultado da execucao da procedure de relatorio. */
public record ReportRunResponse(
        String engine,          // ORACLE_PLSQL ou JAVA_FALLBACK
        int days,
        int generated,
        long elapsedMs,
        List<SummaryReportResponse> reports
) { }
