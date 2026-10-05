package com.smarthas.api.dto;

import com.smarthas.api.domain.SummaryReport;

public record SummaryReportResponse(
        Long id, Long userId, String userName, String startDate, String endDate,
        int measurementCount, Double avgSystolic, Double avgDiastolic, Integer maxSystolic,
        Double controlRate, int openAlerts, String riskLevel, String summary, String generatedAt
) {
    public static SummaryReportResponse from(SummaryReport r, String userName) {
        return new SummaryReportResponse(r.getId(), r.getUserId(), userName,
                r.getStartDate().toString(), r.getEndDate().toString(),
                r.getMeasurementCount(), r.getAvgSystolic(), r.getAvgDiastolic(), r.getMaxSystolic(),
                r.getControlRate(), r.getOpenAlerts(), r.getRiskLevel(), r.getSummary(),
                r.getGeneratedAt().toString());
    }
}
