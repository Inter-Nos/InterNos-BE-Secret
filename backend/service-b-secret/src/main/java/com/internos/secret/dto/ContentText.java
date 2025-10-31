package com.internos.secret.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("TEXT")
public class ContentText {
    @NotBlank
    private String type = "TEXT";
    
    @NotBlank
    @Size(max = 10000)
    private String text;
}

