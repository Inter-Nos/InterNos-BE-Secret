package com.internos.secret.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class IpHashUtil {

    private final String pepper;

    public IpHashUtil(@Value("${app.security.ip-hash-pepper}") String pepper) {
        this.pepper = pepper;
    }

    public String hash(String ip) {
        try {
            // Use HMAC-SHA256 with pepper for one-way hashing
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(pepper.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(ip.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            // Fallback to SHA-256 if HMAC fails
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest((ip + pepper).getBytes(StandardCharsets.UTF_8));
                return Base64.getEncoder().encodeToString(hash);
            } catch (NoSuchAlgorithmException ex) {
                throw new RuntimeException("Failed to hash IP address", ex);
            }
        }
    }
}

