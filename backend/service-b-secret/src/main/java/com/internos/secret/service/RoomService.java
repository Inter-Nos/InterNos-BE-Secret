package com.internos.secret.service;

import com.internos.secret.dto.*;
import com.internos.secret.entity.SecretRoom;
import com.internos.secret.exception.ForbiddenException;
import com.internos.secret.exception.NotFoundException;
import com.internos.secret.repository.AttemptRepository;
import com.internos.secret.repository.SecretRoomRepository;
import com.internos.secret.security.AuthenticationContext;
import com.internos.secret.security.AuthenticationContextHolder;
import com.internos.secret.util.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RoomService {

    private final SecretRoomRepository roomRepository;
    private final AttemptRepository attemptRepository;
    private final PasswordUtil passwordUtil;
    private final AuthenticationContextHolder authContextHolder;
    private final StorageService storageService;

    public RoomService(SecretRoomRepository roomRepository,
                     AttemptRepository attemptRepository,
                     PasswordUtil passwordUtil,
                     AuthenticationContextHolder authContextHolder,
                     StorageService storageService) {
        this.roomRepository = roomRepository;
        this.attemptRepository = attemptRepository;
        this.passwordUtil = passwordUtil;
        this.authContextHolder = authContextHolder;
        this.storageService = storageService;
    }

    @Transactional
    public CreateRoomResp createRoom(CreateRoomReq req) {
        AuthenticationContext auth = authContextHolder.getContext()
                .orElseThrow(() -> new ForbiddenException("Authentication required"));

        // Hash answer
        String answerHash = passwordUtil.hash(req.getAnswer());

        // Build room entity
        SecretRoom.SecretRoomBuilder builder = SecretRoom.builder()
                .ownerId(auth.getUserId())
                .ownerName(auth.getUsername())
                .title(req.getTitle())
                .hint(req.getHint())
                .answerHash(answerHash)
                .contentType(mapContentType(req.getContent()))
                .visibility(req.getVisibility())
                .policy(req.getPolicy())
                .viewLimit(req.getViewLimit())
                .viewsUsed(0)
                .expiresAt(req.getExpiresAt())
                .isActive(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now());

        // Set content based on type
        if (req.getContent() instanceof ContentText) {
            builder.contentText(((ContentText) req.getContent()).getText());
            builder.imageRef(null);
            builder.imageMeta(null);
            builder.alt(null);
        } else if (req.getContent() instanceof ContentImage) {
            ContentImage imageContent = (ContentImage) req.getContent();
            builder.contentText(null);
            builder.imageRef(imageContent.getFileRef());
            builder.alt(imageContent.getAlt());
            // imageMeta is set by post-processing
        }

        SecretRoom room = builder.build();
        room = roomRepository.save(room);

        // Generate share URL
        String shareUrl = generateShareUrl(room.getId());

        return CreateRoomResp.builder()
                .id(room.getId())
                .shareUrl(shareUrl)
                .build();
    }

    public RoomMeta getRoomMeta(Long roomId) {
        Optional<AuthenticationContext> authOpt = authContextHolder.getContext();
        
        SecretRoom room;
        if (authOpt.isPresent()) {
            // If authenticated, allow access to own private rooms
            room = roomRepository.findByIdForOwner(roomId, authOpt.get().getUserId())
                    .orElseThrow(() -> new NotFoundException("Room not found"));
        } else {
            // Public rooms only
            room = roomRepository.findPublicById(roomId)
                    .orElseThrow(() -> new NotFoundException("Room not found"));
        }

        return toRoomMeta(room);
    }

    @Transactional
    public UpdatedResp updateRoom(Long roomId, UpdateRoomReq req) {
        AuthenticationContext auth = authContextHolder.getContext()
                .orElseThrow(() -> new ForbiddenException("Authentication required"));

        SecretRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Room not found"));

        // Check ownership
        if (!room.getOwnerId().equals(auth.getUserId())) {
            throw new ForbiddenException("Not authorized to update this room");
        }

        // Update fields
        if (req.getTitle() != null) room.setTitle(req.getTitle());
        if (req.getHint() != null) room.setHint(req.getHint());
        if (req.getVisibility() != null) room.setVisibility(req.getVisibility());
        if (req.getPolicy() != null) room.setPolicy(req.getPolicy());
        if (req.getViewLimit() != null) room.setViewLimit(req.getViewLimit());
        if (req.getExpiresAt() != null) room.setExpiresAt(req.getExpiresAt());

        roomRepository.save(room);

        return UpdatedResp.builder().updated(true).build();
    }

    @Transactional
    public void deleteRoom(Long roomId) {
        AuthenticationContext auth = authContextHolder.getContext()
                .orElseThrow(() -> new ForbiddenException("Authentication required"));

        SecretRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Room not found"));

        // Check ownership
        if (!room.getOwnerId().equals(auth.getUserId())) {
            throw new ForbiddenException("Not authorized to delete this room");
        }

        roomRepository.delete(room);
    }

    public PublicRoomsResp getPublicRooms(String sort, Integer limit, String cursor) {
        int pageSize = limit != null ? Math.min(limit, 50) : 20;
        Pageable pageable = PageRequest.of(0, pageSize);

        List<SecretRoom> rooms;
        Instant cursorInstant = cursor != null ? Instant.parse(cursor) : null;

        switch (sort != null ? sort : "trending") {
            case "new":
                rooms = cursorInstant != null 
                    ? roomRepository.findPublicRoomsWithCursor(cursorInstant, pageable)
                    : roomRepository.findPublicRoomsOrderByNew(pageable);
                break;
            case "trending":
            case "hard":
            default:
                // For trending and hard, we need to fetch and sort by metrics
                rooms = cursorInstant != null 
                    ? roomRepository.findPublicRoomsWithCursor(cursorInstant, pageable)
                    : roomRepository.findPublicRoomsOrderByNew(pageable);
                // Will sort by metrics after fetching
                break;
        }

        List<PublicRoomCard> cards = rooms.stream()
                .map(room -> toPublicRoomCard(room, sort))
                .collect(Collectors.toList());

        // Sort if needed
        if ("trending".equals(sort) || "hard".equals(sort)) {
            cards.sort((a, b) -> {
                if ("trending".equals(sort)) {
                    // Sort by attempts1h * solveRate1h (trending score)
                    double scoreA = (a.getAttempts1h() != null ? a.getAttempts1h() : 0) * 
                                   (a.getSolveRate1h() != null ? a.getSolveRate1h() : 0);
                    double scoreB = (b.getAttempts1h() != null ? b.getAttempts1h() : 0) * 
                                   (b.getSolveRate1h() != null ? b.getSolveRate1h() : 0);
                    return Double.compare(scoreB, scoreA); // Descending
                } else {
                    // Sort by low solve rate (hard)
                    double rateA = a.getSolveRate1h() != null ? a.getSolveRate1h() : 0;
                    double rateB = b.getSolveRate1h() != null ? b.getSolveRate1h() : 0;
                    return Double.compare(rateA, rateB); // Ascending (lower = harder)
                }
            });
        }

        // Get next cursor
        String nextCursor = null;
        if (!cards.isEmpty()) {
            SecretRoom lastRoom = rooms.get(rooms.size() - 1);
            nextCursor = lastRoom.getCreatedAt().toString();
        }

        return PublicRoomsResp.builder()
                .items(cards)
                .nextCursor(nextCursor)
                .build();
    }

    private PublicRoomCard toPublicRoomCard(SecretRoom room, String sort) {
        Instant oneHourAgo = Instant.now().minusSeconds(3600);
        
        Long attempts1h = attemptRepository.countByRoomIdSince(room.getId(), oneHourAgo);
        Long correct1h = attemptRepository.countCorrectByRoomIdSince(room.getId(), oneHourAgo);
        
        Double solveRate1h = attempts1h > 0 ? (correct1h.doubleValue() / attempts1h.doubleValue()) : 0.0;

        return PublicRoomCard.builder()
                .id(room.getId())
                .title(room.getTitle())
                .hint(room.getHint())
                .ownerName(room.getOwnerName())
                .attempts1h(attempts1h.intValue())
                .solveRate1h(solveRate1h)
                .badge(determineBadge(solveRate1h, attempts1h))
                .contentType(room.getContentType())
                .build();
    }

    private String determineBadge(Double solveRate, Long attempts) {
        if (attempts == 0 || attempts < 5) {
            return "NEW";
        }
        if (solveRate < 0.1) {
            return "HARD";
        }
        if (solveRate > 0.8) {
            return "EASY";
        }
        return "MEDIUM";
    }

    private RoomMeta toRoomMeta(SecretRoom room) {
        String thumbnailUrl = null;
        if (room.getContentType() == SecretRoom.ContentType.IMAGE && room.getImageMeta() != null) {
            Map<String, Object> meta = room.getImageMeta();
            Object thumbRef = meta.get("thumb_ref");
            if (thumbRef != null) {
                thumbnailUrl = storageService.generateThumbnailUrl(thumbRef.toString());
            }
        }

        return RoomMeta.builder()
                .id(room.getId())
                .ownerId(room.getOwnerId())
                .ownerName(room.getOwnerName())
                .title(room.getTitle())
                .hint(room.getHint())
                .visibility(room.getVisibility())
                .policy(room.getPolicy())
                .viewLimit(room.getViewLimit())
                .viewsUsed(room.getViewsUsed())
                .expiresAt(room.getExpiresAt())
                .isActive(room.getIsActive())
                .contentType(room.getContentType())
                .thumbnailUrl(thumbnailUrl)
                .build();
    }

    private SecretRoom.ContentType mapContentType(Content content) {
        if (content instanceof ContentText) {
            return SecretRoom.ContentType.TEXT;
        } else if (content instanceof ContentImage) {
            return SecretRoom.ContentType.IMAGE;
        }
        throw new IllegalArgumentException("Unknown content type");
    }

    private String generateShareUrl(Long roomId) {
        // In production, this would be the actual frontend URL
        return String.format("https://internos.app/s/%d", roomId);
    }
}

