package com.internos.secret.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.internos.secret.entity.SecretRoom;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;

@Data
public class CreateRoomReq {
    @NotBlank
    @Size(min = 2, max = 80)
    private String title;

    @NotBlank
    @Size(min = 2, max = 120)
    private String hint;

    @NotBlank
    private String answer;

    @Valid
    @NotNull
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = ContentText.class, name = "TEXT"),
            @JsonSubTypes.Type(value = ContentImage.class, name = "IMAGE")
    })
    private Content content;

    @NotNull
    private SecretRoom.Visibility visibility;

    @NotNull
    private SecretRoom.Policy policy;

    private Integer viewLimit;

    private Instant expiresAt;
}

