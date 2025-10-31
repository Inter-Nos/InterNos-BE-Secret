package com.internos.secret.exception;

public class RateLimitedException extends RuntimeException {
    private final int retryAfterSec;

    public RateLimitedException(String message, int retryAfterSec) {
        super(message);
        this.retryAfterSec = retryAfterSec;
    }

    public int getRetryAfterSec() {
        return retryAfterSec;
    }
}

