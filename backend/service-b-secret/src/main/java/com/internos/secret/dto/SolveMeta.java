package com.internos.secret.dto;

import com.internos.secret.entity.SecretRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolveMeta {
    private Long id;
    private String title;
    private String hint;
    private SecretRoom.Policy policy;
    private Integer remaining;
    private Integer limit;
    private Instant expiresAt;
    private Boolean locked;
    private Integer retryAfterSec;
}

