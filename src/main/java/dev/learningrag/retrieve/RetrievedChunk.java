package dev.learningrag.retrieve;

import java.util.Map;

import dev.learningrag.core.AccessLevel;

/**
 * A chunk that passed tenant and ACL filtering and is eligible to be shown to the model.
 */
public record RetrievedChunk(
        String chunkId,
        String documentKey,
        String versionLabel,
        String title,
        String content,
        AccessLevel accessLevel,
        double distance,
        Map<String, Object> metadata) {

    public RetrievedChunk {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
