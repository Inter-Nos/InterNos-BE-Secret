package com.internos.secret.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "lockout")
@IdClass(LockoutId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class Lockout {

    @Id
    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Id
    @Column(name = "ip_hash", nullable = false)
    private String ipHash;

    @Column(nullable = false)
    private Instant until;
}

