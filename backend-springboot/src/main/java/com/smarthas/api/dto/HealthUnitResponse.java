package com.smarthas.api.dto;

import com.smarthas.api.domain.HealthUnit;

public record HealthUnitResponse(
        Long id, String name, String type, double latitude, double longitude, String address, boolean active
) {
    public static HealthUnitResponse from(HealthUnit u) {
        return new HealthUnitResponse(u.getId(), u.getName(), u.getType(),
                u.getLatitude(), u.getLongitude(), u.getAddress(), u.isActive());
    }
}
