package com.internos.secret.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PresignReq {
    @NotBlank
    private String fileName;

    @NotBlank
    private String mimeType; // image/jpeg, image/png, image/webp

    @NotNull
    @Max(10485760) // 10MB
    private Long size;
}

