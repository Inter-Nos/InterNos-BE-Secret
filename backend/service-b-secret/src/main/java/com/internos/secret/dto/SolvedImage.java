package com.internos.secret.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("IMAGE")
public class SolvedImage {
    private String type = "IMAGE";
    private String signedUrl;
    private String alt;
}

