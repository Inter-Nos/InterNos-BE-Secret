package com.internos.secret.service;

import com.internos.secret.entity.Lockout;
import com.internos.secret.exception.LockedException;
import com.internos.secret.repository.AttemptRepository;
import com.internos.secret.repository.LockoutRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class LockoutService {

    private static final String FAIL_COUNT_PREFIX = "lockout:fail:";
    
    private final LockoutRepository lockoutRepository;
    private final AttemptRepository attemptRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final int failsThreshold;
    private final int lockoutTtlSec;
    private final int windowSec;

    public LockoutService(LockoutRepository lockoutRepository,
                         AttemptRepository attemptRepository,
                         RedisTemplate<String, Object> redisTemplate,
                         @Value("${app.lockout.fails-threshold}") int failsThreshold,
                         @Value("${app.lockout.ttl-sec}") int lockoutTtlSec) {
        this.lockoutRepository = lockoutRepository;
        this.attemptRepository = attemptRepository;
        this.redisTemplate = redisTemplate;
        this.failsThreshold = failsThreshold;
        this.lockoutTtlSec = lockoutTtlSec;
        this.windowSec = lockoutTtlSec; // Use same window as TTL for simplicity
    }

    public void checkLockout(Long roomId, String ipHash) {
        // Check DB lockout table
        Optional<Lockout> lockout = lockoutRepository.findActiveLockout(roomId, ipHash, Instant.now());
        if (lockout.isPresent()) {
            long remainingSec = java.time.Duration.between(Instant.now(), lockout.get().getUntil()).getSeconds();
            throw new LockedException("Too many failed attempts. Please try again later.", (int) remainingSec);
        }
    }

    @Transactional
    public void recordFailure(Long roomId, String ipHash) {
        Instant since = Instant.now().minusSeconds(windowSec);
        
        // Get fail count from Redis or DB
        String redisKey = FAIL_COUNT_PREFIX + roomId + ":" + ipHash;
        Long failCount = getFailCount(roomId, ipHash, since, redisKey);
        
        failCount++;
        
        // Store in Redis for fast access
        redisTemplate.opsForValue().set(redisKey, failCount, windowSec, TimeUnit.SECONDS);
        
        // If threshold reached, create lockout record
        if (failCount >= failsThreshold) {
            Instant until = Instant.now().plusSeconds(lockoutTtlSec);
            Lockout lockout = Lockout.builder()
                    .roomId(roomId)
                    .ipHash(ipHash)
                    .until(until)
                    .build();
            lockoutRepository.save(lockout);
            log.warn("Lockout created for roomId={}, ipHash={}, until={}", roomId, ipHash, until);
            throw new LockedException("Too many failed attempts. Please try again later.", lockoutTtlSec);
        }
    }

    public void clearFailureCount(Long roomId, String ipHash) {
        String redisKey = FAIL_COUNT_PREFIX + roomId + ":" + ipHash;
        redisTemplate.delete(redisKey);
    }

    private Long getFailCount(Long roomId, String ipHash, Instant since, String redisKey) {
        // Try Redis first
        Object cached = redisTemplate.opsForValue().get(redisKey);
        if (cached instanceof Number) {
            return ((Number) cached).longValue();
        }
        
        // Fallback to DB count
        Long count = attemptRepository.countFailedAttemptsSince(roomId, ipHash, since);
        return count != null ? count : 0L;
    }
}

