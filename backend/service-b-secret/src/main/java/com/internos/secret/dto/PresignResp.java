package com.internos.secret.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresignResp {
    private String uploadUrl;
    private String fileRef;
    private Integer expiresIn;
}

