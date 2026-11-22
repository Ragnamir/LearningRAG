package dev.learningrag.domain;

import java.time.Instant;
import java.util.UUID;

public record Document(
        UUID id,
        String tenantId,
        String documentKey,
        String title,
        UUID currentVersionId,
        Instant createdAt) {
}
