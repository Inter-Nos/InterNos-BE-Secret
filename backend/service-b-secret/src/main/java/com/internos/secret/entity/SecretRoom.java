package com.internos.secret.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "secret_room")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class SecretRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String hint;

    @Column(name = "answer_hash", nullable = false)
    private String answerHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false)
    private ContentType contentType;

    @Column(name = "content_text")
    private String contentText;

    @Column(name = "image_ref")
    private String imageRef;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "image_meta")
    private Map<String, Object> imageMeta;

    @Column(name = "alt")
    private String alt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Visibility visibility;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Policy policy;

    @Column(name = "view_limit")
    private Integer viewLimit;

    @Column(name = "views_used", nullable = false)
    @Builder.Default
    private Integer viewsUsed = 0;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public enum ContentType {
        TEXT, IMAGE
    }

    public enum Visibility {
        PUBLIC, PRIVATE
    }

    public enum Policy {
        ONCE, LIMITED, UNLIMITED
    }
}

