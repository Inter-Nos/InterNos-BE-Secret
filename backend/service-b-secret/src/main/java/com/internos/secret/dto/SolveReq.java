package com.internos.secret.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SolveReq {
    @NotNull
    private Long roomId;

    @NotBlank
    private String answer;

    @NotBlank
    private String nonce;
}

