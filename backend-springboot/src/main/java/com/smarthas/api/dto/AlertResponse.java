package com.smarthas.api.dto;

import com.smarthas.api.domain.Alert;

public record AlertResponse(
        Long id, Long userId, String userName, Long measurementId,
        String type, String severity, String message, String status,
        String createdAt, String resolvedAt
) {
    public static AlertResponse from(Alert a, String userName) {
        return new AlertResponse(a.getId(), a.getUserId(), userName, a.getMeasurementId(),
                a.getType(), a.getSeverity(), a.getMessage(), a.getStatus(),
                a.getCreatedAt() == null ? null : a.getCreatedAt().toString(),
                a.getResolvedAt() == null ? null : a.getResolvedAt().toString());
    }
}
