package dev.learningrag.domain;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import dev.learningrag.core.AccessLevel;

public record DocumentVersion(
        UUID id,
        UUID documentId,
        int revision,
        String versionLabel,
        String contentHash,
        AccessLevel accessLevel,
        Map<String, Object> metadata,
        Instant createdAt) {

    public DocumentVersion {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
