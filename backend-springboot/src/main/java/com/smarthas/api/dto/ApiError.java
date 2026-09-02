package com.smarthas.api.dto;

import java.time.Instant;

/** Corpo padrao de erro. O campo "detail" mantem compatibilidade com os clientes antigos. */
public record ApiError(String detail, int status, String timestamp) {
    public static ApiError of(String detail, int status) {
        return new ApiError(detail, status, Instant.now().toString());
    }
}
