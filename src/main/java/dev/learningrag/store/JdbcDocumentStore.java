package dev.learningrag.store;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import dev.learningrag.domain.Document;
import dev.learningrag.domain.DocumentVersion;

@Repository
public class JdbcDocumentStore implements DocumentStore {

    private final JdbcTemplate jdbc;
    private final JdbcSupport support;

    private final RowMapper<Document> documentMapper = (rs, i) -> new Document(
            JdbcSupport.uuid(rs, "id"),
            rs.getString("tenant_id"),
            rs.getString("document_key"),
            rs.getString("title"),
            JdbcSupport.uuid(rs, "current_version_id"),
            JdbcSupport.instant(rs, "created_at"));

    public JdbcDocumentStore(JdbcTemplate jdbc, JdbcSupport support) {
        this.jdbc = jdbc;
        this.support = support;
    }

    private RowMapper<DocumentVersion> versionMapper() {
        return (rs, i) -> new DocumentVersion(
                JdbcSupport.uuid(rs, "id"),
                JdbcSupport.uuid(rs, "document_id"),
                rs.getInt("revision"),
                rs.getString("version_label"),
                rs.getString("content_hash"),
                JdbcSupport.accessLevel(rs, "access_level"),
                support.readMap(rs, "metadata"),
                JdbcSupport.instant(rs, "created_at"));
    }

    @Override
    public Optional<Document> findDocument(String tenantId, String documentKey) {
        return jdbc.query("SELECT * FROM documents WHERE tenant_id = ? AND document_key = ?",
                documentMapper, tenantId, documentKey).stream().findFirst();
    }

    @Override
    public Optional<Document> findDocumentById(UUID documentId) {
        return jdbc.query("SELECT * FROM documents WHERE id = ?", documentMapper, documentId)
                .stream().findFirst();
    }

    @Override
    public Optional<DocumentVersion> findVersion(UUID versionId) {
        return jdbc.query("SELECT * FROM document_versions WHERE id = ?", versionMapper(), versionId)
                .stream().findFirst();
    }

    @Override
    public Optional<DocumentVersion> findVersionByLabel(UUID documentId, String versionLabel) {
        return jdbc.query("SELECT * FROM document_versions WHERE document_id = ? AND version_label = ?",
                versionMapper(), documentId, versionLabel).stream().findFirst();
    }

    @Override
    public List<DocumentVersion> listVersions(UUID documentId) {
        return jdbc.query("SELECT * FROM document_versions WHERE document_id = ? ORDER BY revision",
                versionMapper(), documentId);
    }

    @Override
    public UUID insertDocument(String tenantId, String documentKey, String title) {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO documents (id, tenant_id, document_key, title)
                VALUES (?, ?, ?, ?)
                """, id, tenantId, documentKey, title);
        return id;
    }

    @Override
    public int nextRevision(UUID documentId) {
        Integer max = jdbc.queryForObject(
                "SELECT COALESCE(MAX(revision), 0) + 1 FROM document_versions WHERE document_id = ?",
                Integer.class, documentId);
        return max == null ? 1 : max;
    }

    @Override
    public UUID insertVersion(UUID documentId, int revision, String versionLabel, String contentHash,
                              dev.learningrag.core.AccessLevel accessLevel,
                              java.util.Map<String, Object> metadata) {
        UUID id = UUID.randomUUID();
        try {
            jdbc.update("""
                    INSERT INTO document_versions
                        (id, document_id, revision, version_label, content_hash, access_level, metadata)
                    VALUES (?, ?, ?, ?, ?, ?, CAST(? AS jsonb))
                    """, id, documentId, revision, versionLabel, contentHash, accessLevel.name(),
                    support.jsonb(metadata));
        } catch (java.sql.SQLException e) {
            throw new IllegalStateException("Cannot serialize version metadata", e);
        }
        return id;
    }

    @Override
    public void setCurrentVersion(UUID documentId, UUID versionId) {
        jdbc.update("UPDATE documents SET current_version_id = ?, updated_at = now() WHERE id = ?",
                versionId, documentId);
    }
}
