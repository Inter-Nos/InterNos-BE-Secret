package com.internos.secret.service;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class StorageService {

    private final Storage storage;
    private final String bucketName;
    private final int signedUrlTtlSec;

    public StorageService(Storage storage,
                         @Value("${app.storage.bucket}") String bucketName,
                         @Value("${app.storage.signed-url-ttl-sec}") int signedUrlTtlSec) {
        this.storage = storage;
        this.bucketName = bucketName;
        this.signedUrlTtlSec = signedUrlTtlSec;
    }

    public UploadResult generateUploadUrl(String fileName, String mimeType) {
        // Generate unique file reference
        String fileRef = generateFileRef(fileName);
        
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileRef)
                .setContentType(mimeType)
                .build();

        try {
            // Generate PUT signed URL for upload
            URL signedUrl = storage.signUrl(
                    blobInfo,
                    signedUrlTtlSec,
                    TimeUnit.SECONDS,
                    Storage.SignUrlOption.httpMethod(com.google.cloud.storage.HttpMethod.PUT),
                    Storage.SignUrlOption.withV4Signature()
            );
            return new UploadResult(signedUrl.toString(), fileRef);
        } catch (StorageException e) {
            log.error("Failed to generate upload signed URL", e);
            throw new RuntimeException("Failed to generate upload URL", e);
        }
    }

    public static class UploadResult {
        private final String uploadUrl;
        private final String fileRef;

        public UploadResult(String uploadUrl, String fileRef) {
            this.uploadUrl = uploadUrl;
            this.fileRef = fileRef;
        }

        public String getUploadUrl() {
            return uploadUrl;
        }

        public String getFileRef() {
            return fileRef;
        }
    }

    public String generateReadUrl(String fileRef) {
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileRef).build();

        try {
            // Generate GET signed URL for reading
            URL signedUrl = storage.signUrl(
                    blobInfo,
                    signedUrlTtlSec,
                    TimeUnit.SECONDS,
                    Storage.SignUrlOption.httpMethod(com.google.cloud.storage.HttpMethod.GET),
                    Storage.SignUrlOption.withV4Signature()
            );
            return signedUrl.toString();
        } catch (StorageException e) {
            log.error("Failed to generate read signed URL for fileRef: {}", fileRef, e);
            throw new RuntimeException("Failed to generate read URL", e);
        }
    }

    public String generateThumbnailUrl(String thumbRef) {
        if (thumbRef == null || thumbRef.isEmpty()) {
            return null;
        }
        return generateReadUrl(thumbRef);
    }

    private String generateFileRef(String fileName) {
        // Generate unique file reference: timestamp/uuid/filename
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String sanitizedFileName = sanitizeFileName(fileName);
        return String.format("%s/%s/%s", timestamp, uuid, sanitizedFileName);
    }

    private String sanitizeFileName(String fileName) {
        // Remove path separators and keep only filename
        String name = fileName;
        if (name.contains("/")) {
            name = name.substring(name.lastIndexOf("/") + 1);
        }
        // Remove any potentially dangerous characters
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}

