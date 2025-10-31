package com.internos.secret.controller;

import com.internos.secret.dto.PresignReq;
import com.internos.secret.dto.PresignResp;
import com.internos.secret.service.StorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
@Slf4j
public class UploadController {

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final StorageService storageService;

    @Value("${app.storage.signed-url-ttl-sec}")
    private int signedUrlTtlSec;

    @PostMapping("/presign")
    public ResponseEntity<PresignResp> presign(@Valid @RequestBody PresignReq req) {
        // Validate mime type
        if (!ALLOWED_MIME_TYPES.contains(req.getMimeType())) {
            throw new IllegalArgumentException("Unsupported MIME type: " + req.getMimeType());
        }

        // Validate size (max 10MB)
        if (req.getSize() > 10485760) {
            throw new IllegalArgumentException("File size exceeds 10MB limit");
        }

        // Generate upload URL and file reference
        StorageService.UploadResult result = storageService.generateUploadUrl(req.getFileName(), req.getMimeType());

        PresignResp resp = PresignResp.builder()
                .uploadUrl(result.getUploadUrl())
                .fileRef(result.getFileRef())
                .expiresIn(signedUrlTtlSec)
                .build();

        return ResponseEntity.ok(resp);
    }
}

