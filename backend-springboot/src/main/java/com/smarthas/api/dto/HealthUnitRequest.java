package com.smarthas.api.dto;

import jakarta.validation.constraints.NotBlank;

public record HealthUnitRequest(
        @NotBlank(message = "Nome e obrigatorio") String name,
        @NotBlank(message = "Tipo e obrigatorio") String type,
        double latitude,
        double longitude,
        String address,
        Boolean active
) { }
