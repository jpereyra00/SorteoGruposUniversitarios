package com.uade.exammanager.controller;

import com.uade.exammanager.exception.BusinessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException exception) {
        return buildError(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException exception) {
        String fallbackMessage = "No se pudo guardar la operación porque hay datos duplicados o una relación inválida.";
        String detail = exception.getMostSpecificCause() != null ? exception.getMostSpecificCause().getMessage() : "";

        if (detail != null && detail.toLowerCase().contains("group_members")) {
            fallbackMessage = "No se pudo asignar el estudiante porque ya pertenece a otro grupo. Libere al estudiante o limpie grupos antes de reintentar.";
        }

        return buildError(HttpStatus.CONFLICT, fallbackMessage);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> "Campo inválido: " + error.getField())
                .orElse("Datos inválidos en la solicitud.");

        return buildError(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception exception) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado: " + exception.getMessage());
    }

    private ResponseEntity<Map<String, Object>> buildError(HttpStatus status, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", status.value());
        response.put("message", message);
        return ResponseEntity.status(status).body(response);
    }
}
