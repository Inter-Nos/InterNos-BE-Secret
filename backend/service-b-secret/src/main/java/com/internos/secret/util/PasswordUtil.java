package com.internos.secret.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordUtil {

    private final PasswordEncoder passwordEncoder;

    public PasswordUtil() {
        // BCrypt with strength 12 (minimum as per spec)
        this.passwordEncoder = new BCryptPasswordEncoder(12);
    }

    public String hash(String plainText) {
        return passwordEncoder.encode(plainText);
    }

    public boolean matches(String plainText, String hash) {
        return passwordEncoder.matches(plainText, hash);
    }
}

