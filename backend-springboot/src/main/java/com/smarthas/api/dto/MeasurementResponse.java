package com.smarthas.api.dto;

import com.smarthas.api.domain.Alert;
import com.smarthas.api.domain.Classification;
import com.smarthas.api.domain.Measurement;

/**
 * Saida de uma medicao, ja com a classificacao calculada no servidor.
 * Fase 6: inclui origem, frequencia cardiaca e o alerta gerado pela regra no banco (se houver).
 */
public record MeasurementResponse(
        Long id,
        int systolic,
        int diastolic,
        Integer heartRate,
        String date,
        String time,
        String notes,
        String source,
        String createdAt,
        String classification,
        String classificationLabel,
        String colorHex,
        String alertSeverity,
        String alertMessage
) {
    public static MeasurementResponse from(Measurement m) {
        return from(m, null);
    }

    public static MeasurementResponse from(Measurement m, Alert alert) {
        Classification c = m.getClassification();
        return new MeasurementResponse(
                m.getId(), m.getSystolic(), m.getDiastolic(), m.getHeartRate(), m.getDate(), m.getTime(),
                m.getNotes(), m.getSource(), m.getCreatedAt().toString(),
                c.name(), c.getLabel(), c.getColorHex(),
                alert == null ? null : alert.getSeverity(),
                alert == null ? null : alert.getMessage()
        );
    }
}
