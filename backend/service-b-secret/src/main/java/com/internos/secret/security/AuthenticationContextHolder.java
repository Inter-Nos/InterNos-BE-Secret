package com.internos.secret.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthenticationContextHolder {

    private static final String USER_ID_ATTRIBUTE = "userId";
    private static final String USERNAME_ATTRIBUTE = "username";

    public Optional<AuthenticationContext> getContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Long userId = getUserId(authentication);
        String username = getUsername(authentication);

        if (userId == null || username == null) {
            return Optional.empty();
        }

        return Optional.of(AuthenticationContext.builder()
                .userId(userId)
                .username(username)
                .authenticated(true)
                .build());
    }

    private Long getUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> attributes = (java.util.Map<String, Object>) principal;
            Object userIdObj = attributes.get(USER_ID_ATTRIBUTE);
            if (userIdObj instanceof Number) {
                return ((Number) userIdObj).longValue();
            }
        }
        return null;
    }

    private String getUsername(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> attributes = (java.util.Map<String, Object>) principal;
            Object usernameObj = attributes.get(USERNAME_ATTRIBUTE);
            if (usernameObj instanceof String) {
                return (String) usernameObj;
            }
        }
        // Fallback to authentication name
        return authentication.getName();
    }
}

