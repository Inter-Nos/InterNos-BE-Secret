package com.internos.secret.controller;

import com.internos.secret.dto.*;
import com.internos.secret.service.NonceService;
import com.internos.secret.service.SolveService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class SolveController {

    private final SolveService solveService;
    private final NonceService nonceService;
    private final int nonceTtlSec;

    public SolveController(SolveService solveService,
                          NonceService nonceService,
                          @Value("${app.solve.nonce-ttl-sec}") int nonceTtlSec) {
        this.solveService = solveService;
        this.nonceService = nonceService;
        this.nonceTtlSec = nonceTtlSec;
    }

    @GetMapping("/s/{id}/meta")
    public ResponseEntity<SolveMeta> getSolveMeta(@PathVariable Long id,
                                                  HttpServletRequest request) {
        String clientIp = getClientIp(request);
        SolveMeta meta = solveService.getSolveMeta(id, clientIp);
        return ResponseEntity.ok(meta);
    }

    @GetMapping("/solve/nonce")
    public ResponseEntity<NonceResp> getNonce(@RequestParam Long roomId) {
        String nonce = nonceService.generateNonce(roomId);
        NonceResp resp = NonceResp.builder()
                .nonce(nonce)
                .expiresIn(nonceTtlSec)
                .build();
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/solve")
    public ResponseEntity<SolveResp> solve(@Valid @RequestBody SolveReq req,
                                          HttpServletRequest request,
                                          @RequestHeader(value = "X-Solver-Anon-Id", required = false) String solverAnonId) {
        String clientIp = getClientIp(request);
        SolveResp resp = solveService.solve(req, clientIp, solverAnonId);
        return ResponseEntity.ok(resp);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Handle comma-separated IPs (X-Forwarded-For can have multiple)
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}

