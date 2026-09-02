package com.smarthas.api.web;

import org.springframework.http.HttpStatus;

/** Excecao de negocio com status HTTP associado. */
public class ApiException extends RuntimeException {
    private final HttpStatus status;

    public ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }
}
