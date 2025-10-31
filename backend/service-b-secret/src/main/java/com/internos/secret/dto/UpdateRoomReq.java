package com.internos.secret.dto;

import com.internos.secret.entity.SecretRoom;
import lombok.Data;

import java.time.Instant;

@Data
public class UpdateRoomReq {
    private String title;
    private String hint;
    private SecretRoom.Visibility visibility;
    private SecretRoom.Policy policy;
    private Integer viewLimit;
    private Instant expiresAt;
}

