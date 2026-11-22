package dev.learningrag.domain;

import java.util.Map;
import java.util.UUID;

import dev.learningrag.core.AccessLevel;

public record StoredChunk(
        UUID id,
        UUID documentVersionId,
        int chunkIndex,
        String content,
        String contentHash,
        int startOffset,
        int endOffset,
        int paragraphStart,
        int paragraphEnd,
        AccessLevel accessLevel,
        Map<String, Object> metadata) {

    public StoredChunk {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
