package com.internos.secret.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private Error error;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Error {
        private String code;
        private String message;
        private Map<String, Object> details;
    }

    public static ErrorResponse of(String code, String message) {
        return ErrorResponse.builder()
                .error(Error.builder()
                        .code(code)
                        .message(message)
                        .build())
                .build();
    }

    public static ErrorResponse of(String code, String message, Map<String, Object> details) {
        return ErrorResponse.builder()
                .error(Error.builder()
                        .code(code)
                        .message(message)
                        .details(details)
                        .build())
                .build();
    }
}

