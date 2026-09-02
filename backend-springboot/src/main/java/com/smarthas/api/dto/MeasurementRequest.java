package com.smarthas.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record MeasurementRequest(
        @Min(value = 50, message = "Sistolica muito baixa") @Max(value = 300, message = "Sistolica muito alta") int systolic,
        @Min(value = 30, message = "Diastolica muito baixa") @Max(value = 200, message = "Diastolica muito alta") int diastolic,
        @NotBlank(message = "Data e obrigatoria") String date,
        @NotBlank(message = "Hora e obrigatoria") String time,
        String notes
) { }
