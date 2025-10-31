package com.internos.secret.dto;

import com.internos.secret.entity.SecretRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicRoomCard {
    private Long id;
    private String title;
    private String hint;
    private String ownerName;
    private Integer attempts1h;
    private Double solveRate1h;
    private String badge;
    private SecretRoom.ContentType contentType;
}

