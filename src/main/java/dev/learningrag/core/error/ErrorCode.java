package dev.learningrag.core.error;

import org.springframework.http.HttpStatus;

/**
 * Error codes are safe to expose. They never carry document content, prompts or provider payloads.
 */
public enum ErrorCode {

    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED),
    INPUT_REJECTED(HttpStatus.BAD_REQUEST),
    NOT_FOUND(HttpStatus.NOT_FOUND),
    CONFLICT(HttpStatus.CONFLICT),
    EGRESS_BLOCKED(HttpStatus.UNPROCESSABLE_ENTITY),
    OUTPUT_REJECTED(HttpStatus.UNPROCESSABLE_ENTITY),
    BUDGET_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS),
    UPSTREAM_UNAVAILABLE(HttpStatus.BAD_GATEWAY),
    UPSTREAM_INVALID_RESPONSE(HttpStatus.BAD_GATEWAY),
    INTERNAL(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
