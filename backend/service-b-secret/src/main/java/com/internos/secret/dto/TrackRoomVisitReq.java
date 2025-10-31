package com.internos.secret.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TrackRoomVisitReq {
    @NotNull
    private Long roomId;
    private String visitorAnonId;
    private String ua;
}

