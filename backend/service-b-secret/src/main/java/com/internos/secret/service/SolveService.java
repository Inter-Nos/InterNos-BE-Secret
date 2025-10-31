package com.internos.secret.service;

import com.internos.secret.dto.*;
import com.internos.secret.dto.SolveResp.SolvedContent;
import com.internos.secret.entity.Attempt;
import com.internos.secret.entity.SecretRoom;
import com.internos.secret.exception.*;
import com.internos.secret.repository.AttemptRepository;
import com.internos.secret.repository.SecretRoomRepository;
import com.internos.secret.util.IpHashUtil;
import com.internos.secret.util.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class SolveService {

    private final SecretRoomRepository roomRepository;
    private final AttemptRepository attemptRepository;
    private final LockoutService lockoutService;
    private final NonceService nonceService;
    private final PasswordUtil passwordUtil;
    private final StorageService storageService;
    private final IpHashUtil ipHashUtil;

    public SolveService(SecretRoomRepository roomRepository,
                       AttemptRepository attemptRepository,
                       LockoutService lockoutService,
                       NonceService nonceService,
                       PasswordUtil passwordUtil,
                       StorageService storageService,
                       IpHashUtil ipHashUtil) {
        this.roomRepository = roomRepository;
        this.attemptRepository = attemptRepository;
        this.lockoutService = lockoutService;
        this.nonceService = nonceService;
        this.passwordUtil = passwordUtil;
        this.storageService = storageService;
        this.ipHashUtil = ipHashUtil;
    }

    @Transactional
    public SolveResp solve(SolveReq req, String clientIp, String solverAnonId) {
        long startTime = System.currentTimeMillis();
        
        // Validate nonce
        Long roomIdFromNonce = nonceService.validateAndConsumeNonce(req.getNonce());
        if (roomIdFromNonce == null || !roomIdFromNonce.equals(req.getRoomId())) {
            throw new NotFoundException("Invalid or expired nonce");
        }

        // Get room
        SecretRoom room = roomRepository.findById(req.getRoomId())
                .orElseThrow(() -> new NotFoundException("Room not found"));

        // Check if room is active
        if (!room.getIsActive()) {
            throw new GoneException("Room is no longer active");
        }

        // Check expiration
        if (room.getExpiresAt() != null && room.getExpiresAt().isBefore(Instant.now())) {
            room.setIsActive(false);
            roomRepository.save(room);
            throw new GoneException("Room has expired");
        }

        // Hash IP
        String ipHash = ipHashUtil.hash(clientIp);

        // Check lockout
        lockoutService.checkLockout(req.getRoomId(), ipHash);

        // Verify answer
        boolean isCorrect = passwordUtil.matches(req.getAnswer(), room.getAnswerHash());

        // Calculate latency
        int latencyMs = (int) (System.currentTimeMillis() - startTime);

        // Record attempt
        Attempt attempt = Attempt.builder()
                .roomId(req.getRoomId())
                .solverAnonId(solverAnonId)
                .isCorrect(isCorrect)
                .latencyMs(latencyMs)
                .ipHash(ipHash)
                .createdAt(Instant.now())
                .build();
        attemptRepository.save(attempt);

        if (!isCorrect) {
            // Record failure and check for lockout
            try {
                lockoutService.recordFailure(req.getRoomId(), ipHash);
            } catch (LockedException e) {
                throw e; // Re-throw lockout exception
            }
            // Don't reveal that the room exists, but answer is wrong
            throw new NotFoundException("Room not found or incorrect answer");
        }

        // Success - clear failure count
        lockoutService.clearFailureCount(req.getRoomId(), ipHash);

        // Apply policy
        applyPolicy(room);

        // Generate content response
        SolvedContent content = generateContent(room);

        // Build policy state
        SolveResp.PolicyState policyState = buildPolicyState(room);

        return SolveResp.builder()
                .ok(true)
                .content(content)
                .policyState(policyState)
                .build();
    }

    private void applyPolicy(SecretRoom room) {
        switch (room.getPolicy()) {
            case ONCE:
                // Mark as used up immediately
                room.setViewsUsed(room.getViewLimit() != null ? room.getViewLimit() : 1);
                room.setIsActive(false);
                break;
            case LIMITED:
                room.setViewsUsed(room.getViewsUsed() + 1);
                if (room.getViewLimit() != null && room.getViewsUsed() >= room.getViewLimit()) {
                    room.setIsActive(false);
                }
                break;
            case UNLIMITED:
                // No change needed
                break;
        }
        roomRepository.save(room);
    }

    private SolvedContent generateContent(SecretRoom room) {
        if (room.getContentType() == SecretRoom.ContentType.TEXT) {
            return SolvedText.builder()
                    .type("TEXT")
                    .text(room.getContentText())
                    .build();
        } else {
            // IMAGE - generate signed URL
            String signedUrl = storageService.generateReadUrl(room.getImageRef());
            return SolvedImage.builder()
                    .type("IMAGE")
                    .signedUrl(signedUrl)
                    .alt(room.getAlt())
                    .build();
        }
    }

    private SolveResp.PolicyState buildPolicyState(SecretRoom room) {
        Integer remaining = null;
        if (room.getPolicy() == SecretRoom.Policy.LIMITED && room.getViewLimit() != null) {
            remaining = Math.max(0, room.getViewLimit() - room.getViewsUsed());
        }

        return SolveResp.PolicyState.builder()
                .policy(room.getPolicy())
                .remaining(remaining)
                .limit(room.getViewLimit())
                .expiresAt(room.getExpiresAt())
                .build();
    }

    public SolveMeta getSolveMeta(Long roomId, String clientIp) {
        SecretRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Room not found"));

        // Check if room is active
        if (!room.getIsActive() || 
            (room.getExpiresAt() != null && room.getExpiresAt().isBefore(Instant.now()))) {
            throw new GoneException("Room is no longer available");
        }

        String ipHash = ipHashUtil.hash(clientIp);
        
        // Check lockout
        boolean locked = false;
        Integer retryAfterSec = null;
        
        try {
            lockoutService.checkLockout(roomId, ipHash);
        } catch (LockedException e) {
            locked = true;
            retryAfterSec = e.getRetryAfterSec();
        }

        // Calculate remaining
        Integer remaining = null;
        if (room.getPolicy() == SecretRoom.Policy.LIMITED && room.getViewLimit() != null) {
            remaining = Math.max(0, room.getViewLimit() - room.getViewsUsed());
        }

        return SolveMeta.builder()
                .id(room.getId())
                .title(room.getTitle())
                .hint(room.getHint())
                .policy(room.getPolicy())
                .remaining(remaining)
                .limit(room.getViewLimit())
                .expiresAt(room.getExpiresAt())
                .locked(locked)
                .retryAfterSec(retryAfterSec)
                .build();
    }
}

