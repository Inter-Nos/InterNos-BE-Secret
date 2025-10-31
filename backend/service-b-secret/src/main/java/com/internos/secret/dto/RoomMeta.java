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
public class RoomMeta {
    private Long id;
    private Long ownerId;
    private String ownerName;
    private String title;
    private String hint;
    private SecretRoom.Visibility visibility;
    private SecretRoom.Policy policy;
    private Integer viewLimit;
    private Integer viewsUsed;
    private Instant expiresAt;
    private Boolean isActive;
    private SecretRoom.ContentType contentType;
    private String thumbnailUrl;
}

