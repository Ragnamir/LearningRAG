package dev.learningrag.store;

import java.util.List;
import java.util.UUID;

import dev.learningrag.core.AccessLevel;
import dev.learningrag.domain.StoredChunk;

public interface ChunkStore {

    void insertAll(UUID documentVersionId, String tenantId, AccessLevel accessLevel,
                   List<ChunkToWrite> chunks);

    List<StoredChunk> findByVersion(UUID documentVersionId);

    List<ChunkSearchHit> searchCurrentVersions(String tenantId, List<AccessLevel> levels,
                                               float[] queryEmbedding, int limit);

    record ChunkToWrite(
            int index,
            String content,
            String contentHash,
            int startOffset,
            int endOffset,
            int paragraphStart,
            int paragraphEnd,
            float[] embedding,
            java.util.Map<String, Object> metadata) {
    }

    record ChunkSearchHit(
            UUID chunkId,
            String documentKey,
            String title,
            String versionLabel,
            AccessLevel accessLevel,
            String content,
            java.util.Map<String, Object> metadata,
            double cosineDistance) {
    }
}
