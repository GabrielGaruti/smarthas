package com.smarthas.api.web;

import com.smarthas.api.dto.ApiError;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

/** Converte excecoes em respostas JSON padronizadas (campo "detail"). */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApi(ApiException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(ApiError.of(ex.getMessage(), ex.getStatus().value()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        FieldError fe = ex.getBindingResult().getFieldError();
        String msg = fe != null ? fe.getDefaultMessage() : "Dados invalidos";
        return ResponseEntity.badRequest().body(ApiError.of(msg, 400));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiError.of("Acesso negado", 403));
    }

    /**
     * Erros vindos do banco. Os RAISE_APPLICATION_ERROR das rotinas PL/SQL (ORA-20000 a ORA-20999)
     * sao erros de negocio: viram HTTP 400 com a mensagem da procedure, sem o stack do Oracle.
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiError> handleDatabase(DataAccessException ex) {
        Throwable t = ex;
        while (t != null) {
            if (t instanceof SQLException sql && sql.getErrorCode() >= 20000 && sql.getErrorCode() <= 20999) {
                String msg = sql.getMessage();
                int colon = msg.indexOf(':');
                int newline = msg.indexOf('\n');
                String clean = msg.substring(colon + 1, newline > colon ? newline : msg.length()).trim();
                return ResponseEntity.badRequest().body(ApiError.of(clean, 400));
            }
            t = t.getCause();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of("Erro de acesso ao banco de dados", 500));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of("Erro interno: " + ex.getMessage(), 500));
    }
}
