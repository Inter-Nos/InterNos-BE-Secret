package com.internos.secret.exception;

public class LockedException extends RuntimeException {
    private final int retryAfterSec;

    public LockedException(String message, int retryAfterSec) {
        super(message);
        this.retryAfterSec = retryAfterSec;
    }

    public int getRetryAfterSec() {
        return retryAfterSec;
    }
}

