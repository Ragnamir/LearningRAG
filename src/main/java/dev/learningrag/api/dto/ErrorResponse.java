package dev.learningrag.api.dto;

public record ErrorResponse(
        String requestId,
        String code,
        String message) {
}
