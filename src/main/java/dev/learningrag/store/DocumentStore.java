package dev.learningrag.store;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.learningrag.core.AccessLevel;
import dev.learningrag.domain.Document;
import dev.learningrag.domain.DocumentVersion;

public interface DocumentStore {

    Optional<Document> findDocument(String tenantId, String documentKey);

    Optional<Document> findDocumentById(UUID documentId);

    Optional<DocumentVersion> findVersion(UUID versionId);

    Optional<DocumentVersion> findVersionByLabel(UUID documentId, String versionLabel);

    List<DocumentVersion> listVersions(UUID documentId);

    UUID insertDocument(String tenantId, String documentKey, String title);

    int nextRevision(UUID documentId);

    UUID insertVersion(UUID documentId, int revision, String versionLabel, String contentHash,
                       AccessLevel accessLevel, java.util.Map<String, Object> metadata);

    void setCurrentVersion(UUID documentId, UUID versionId);
}
