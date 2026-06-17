package com.payflow.api.exception;

public class IdempotencyKeyMissingException extends RuntimeException {
    // Add this constructor so it can accept a custom message string:
    public IdempotencyKeyMissingException(String message) {
        super(message);
    }

    // (Optional) Keep your default no-arg constructor if needed elsewhere:
    public IdempotencyKeyMissingException() {
        super();
    }
}
