package com.internos.secret.controller;

import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
@Slf4j
public class HealthController {

    private final JdbcTemplate jdbcTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final Storage storage;

    @GetMapping("/liveness")
    public ResponseEntity<Map<String, String>> liveness() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/readiness")
    public ResponseEntity<Map<String, Object>> readiness() {
        Map<String, Object> response = new HashMap<>();
        boolean allHealthy = true;

        // Check database
        boolean dbHealthy = checkDatabase();
        response.put("database", dbHealthy ? "UP" : "DOWN");

        // Check Redis
        boolean redisHealthy = checkRedis();
        response.put("redis", redisHealthy ? "UP" : "DOWN");

        // Check GCS
        boolean gcsHealthy = checkGcs();
        response.put("gcs", gcsHealthy ? "UP" : "DOWN");

        if (dbHealthy && redisHealthy && gcsHealthy) {
            response.put("status", "READY");
            return ResponseEntity.ok(response);
        } else {
            allHealthy = false;
            response.put("status", "NOT_READY");
            return ResponseEntity.status(503).body(response);
        }
    }

    private boolean checkDatabase() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return true;
        } catch (Exception e) {
            log.error("Database health check failed", e);
            return false;
        }
    }

    private boolean checkRedis() {
        try {
            redisTemplate.opsForValue().set("health:check", "ok", java.time.Duration.ofSeconds(1));
            return Boolean.TRUE.equals(redisTemplate.hasKey("health:check"));
        } catch (Exception e) {
            log.error("Redis health check failed", e);
            return false;
        }
    }

    private boolean checkGcs() {
        try {
            // Simple check - try to get storage service
            storage.list("health-check", Storage.BlobListOption.pageSize(1));
            return true;
        } catch (Exception e) {
            log.error("GCS health check failed", e);
            return false;
        }
    }
}

