package com.payflow.api.exception;

public class IdempotencyKeyMissingException extends RuntimeException {
    public IdempotencyKeyMissingException() {
        super("Idempotency-Key header is required");
    }
}
