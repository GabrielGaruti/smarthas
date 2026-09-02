package com.smarthas.api.dto;

import com.smarthas.api.domain.Classification;
import com.smarthas.api.domain.Measurement;

/** Saida de uma medicao, ja com a classificacao calculada no servidor. */
public record MeasurementResponse(
        Long id,
        int systolic,
        int diastolic,
        String date,
        String time,
        String notes,
        String createdAt,
        String classification,
        String classificationLabel,
        String colorHex
) {
    public static MeasurementResponse from(Measurement m) {
        Classification c = m.getClassification();
        return new MeasurementResponse(
                m.getId(), m.getSystolic(), m.getDiastolic(), m.getDate(), m.getTime(),
                m.getNotes(), m.getCreatedAt().toString(),
                c.name(), c.getLabel(), c.getColorHex()
        );
    }
}
