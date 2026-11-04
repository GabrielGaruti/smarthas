package com.smarthas.api.integration;

import java.time.LocalDateTime;

/** Linha devolvida pelo REF CURSOR da busca dinamica. */
public record MeasurementSearchRow(
        Long id,
        Long userId,
        String patientName,
        int systolic,
        int diastolic,
        Integer heartRate,
        LocalDateTime measuredAt,
        String source,
        String classification
) { }
