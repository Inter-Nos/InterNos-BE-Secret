package com.internos.secret.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
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
public class SolveResp {
    private Boolean ok;

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = SolvedText.class, name = "TEXT"),
            @JsonSubTypes.Type(value = SolvedImage.class, name = "IMAGE")
    })
    private SolvedContent content;

    private PolicyState policyState;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PolicyState {
        private SecretRoom.Policy policy;
        private Integer remaining;
        private Integer limit;
        private Instant expiresAt;
    }

    public interface SolvedContent {
        String getType();
    }
}

