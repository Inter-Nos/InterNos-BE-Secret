package com.internos.secret.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class NonceService {

    private static final String NONCE_PREFIX = "nonce:";
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final int nonceTtlSec;

    public NonceService(RedisTemplate<String, Object> redisTemplate,
                       @Value("${app.solve.nonce-ttl-sec}") int nonceTtlSec) {
        this.redisTemplate = redisTemplate;
        this.nonceTtlSec = nonceTtlSec;
    }

    public String generateNonce(Long roomId) {
        String nonce = UUID.randomUUID().toString();
        String key = NONCE_PREFIX + nonce;
        
        // Store nonce -> roomId mapping with TTL
        redisTemplate.opsForValue().set(key, roomId, nonceTtlSec, TimeUnit.SECONDS);
        
        return nonce;
    }

    public Long validateAndConsumeNonce(String nonce) {
        String key = NONCE_PREFIX + nonce;
        Object value = redisTemplate.opsForValue().get(key);
        
        if (value == null) {
            return null;
        }
        
        // Consume nonce (delete after use)
        redisTemplate.delete(key);
        
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        
        return null;
    }

    public boolean isValidNonce(String nonce) {
        String key = NONCE_PREFIX + nonce;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}

