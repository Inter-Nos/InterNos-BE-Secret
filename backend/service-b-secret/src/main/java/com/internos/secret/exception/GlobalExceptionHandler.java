package com.internos.secret.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse> handleLocked(LockedException e) {
        log.warn("Locked exception: {}", e.getMessage());
        ErrorResponse error = ErrorResponse.of("LOCKED", e.getMessage(), Map.of("retryAfterSec", e.getRetryAfterSec()));
        return ResponseEntity.status(423)
                .header("Retry-After", String.valueOf(e.getRetryAfterSec()))
                .body(error);
    }

    @ExceptionHandler(RateLimitedException.class)
    public ResponseEntity<ErrorResponse> handleRateLimited(RateLimitedException e) {
        log.warn("Rate limited exception: {}", e.getMessage());
        ErrorResponse error = ErrorResponse.of("RATE_LIMITED", e.getMessage(), Map.of("retryAfterSec", e.getRetryAfterSec()));
        return ResponseEntity.status(429)
                .header("Retry-After", String.valueOf(e.getRetryAfterSec()))
                .body(error);
    }

    @ExceptionHandler(GoneException.class)
    public ResponseEntity<ErrorResponse> handleGone(GoneException e) {
        log.warn("Gone exception: {}", e.getMessage());
        ErrorResponse error = ErrorResponse.of("GONE", e.getMessage());
        return ResponseEntity.status(410).body(error);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException e) {
        log.warn("Forbidden exception: {}", e.getMessage());
        ErrorResponse error = ErrorResponse.of("FORBIDDEN", e.getMessage());
        return ResponseEntity.status(403).body(error);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException e) {
        log.debug("Not found exception: {}", e.getMessage());
        ErrorResponse error = ErrorResponse.of("NOT_FOUND", e.getMessage());
        return ResponseEntity.status(404).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        ErrorResponse error = ErrorResponse.of("VALIDATION_ERROR", "Validation failed", errors);
        return ResponseEntity.status(400).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Illegal argument exception: {}", e.getMessage());
        ErrorResponse error = ErrorResponse.of("VALIDATION_ERROR", e.getMessage());
        return ResponseEntity.status(400).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception e) {
        log.error("Unexpected exception", e);
        ErrorResponse error = ErrorResponse.of("INTERNAL_ERROR", "An internal error occurred");
        return ResponseEntity.status(500).body(error);
    }
}

