package com.smarthas.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Entrada de medicao. Campos novos na Fase 6 (opcionais, compativeis com os clientes antigos):
 * heartRate (frequencia cardiaca) e deviceId (leitura enviada por um sensor IoT).
 */
public record MeasurementRequest(
        @Min(value = 50, message = "Sistolica muito baixa") @Max(value = 300, message = "Sistolica muito alta") int systolic,
        @Min(value = 30, message = "Diastolica muito baixa") @Max(value = 200, message = "Diastolica muito alta") int diastolic,
        @NotBlank(message = "Data e obrigatoria")
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Data deve estar no formato AAAA-MM-DD") String date,
        @NotBlank(message = "Hora e obrigatoria")
        @Pattern(regexp = "\\d{2}:\\d{2}", message = "Hora deve estar no formato HH:MM") String time,
        String notes,
        @Min(value = 30, message = "Frequencia cardiaca muito baixa") @Max(value = 220, message = "Frequencia cardiaca muito alta") Integer heartRate,
        Long deviceId
) { }
