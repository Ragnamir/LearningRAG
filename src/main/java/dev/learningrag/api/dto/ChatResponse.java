package dev.learningrag.api.dto;

import java.util.List;

public record ChatResponse(
        String requestId,
        String answer,
        List<CitationDto> citations) {
}
