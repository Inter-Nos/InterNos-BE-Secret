package com.internos.secret.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("IMAGE")
public class ContentImage {
    @NotBlank
    private String type = "IMAGE";
    
    @NotBlank
    private String fileRef;
    
    private String alt;
}

