package dev.learningrag.store;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import dev.learningrag.core.AccessLevel;
import dev.learningrag.domain.StoredChunk;

@Repository
public class JdbcChunkStore implements ChunkStore {

    private final JdbcTemplate jdbc;
    private final JdbcSupport support;

    public JdbcChunkStore(JdbcTemplate jdbc, JdbcSupport support) {
        this.jdbc = jdbc;
        this.support = support;
    }

    private RowMapper<StoredChunk> chunkMapper() {
        return (rs, i) -> new StoredChunk(
                JdbcSupport.uuid(rs, "id"),
                JdbcSupport.uuid(rs, "document_version_id"),
                rs.getInt("chunk_index"),
                rs.getString("content"),
                rs.getString("content_hash"),
                rs.getInt("start_offset"),
                rs.getInt("end_offset"),
                rs.getInt("paragraph_start"),
                rs.getInt("paragraph_end"),
                JdbcSupport.accessLevel(rs, "access_level"),
                support.readMap(rs, "metadata"));
    }

    @Override
    public void insertAll(UUID documentVersionId, String tenantId, AccessLevel accessLevel,
                          List<ChunkToWrite> chunks) {
        jdbc.batchUpdate("""
                INSERT INTO chunks (id, document_version_id, tenant_id, access_level, chunk_index, content,
                                    content_hash, start_offset, end_offset, paragraph_start, paragraph_end,
                                    metadata, embedding)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CAST(? AS jsonb), CAST(? AS vector))
                """, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ChunkToWrite chunk = chunks.get(i);
                ps.setObject(1, UUID.randomUUID());
                ps.setObject(2, documentVersionId);
                ps.setString(3, tenantId);
                ps.setString(4, accessLevel.name());
                ps.setInt(5, chunk.index());
                ps.setString(6, chunk.content());
                ps.setString(7, chunk.contentHash());
                ps.setInt(8, chunk.startOffset());
                ps.setInt(9, chunk.endOffset());
                ps.setInt(10, chunk.paragraphStart());
                ps.setInt(11, chunk.paragraphEnd());
                ps.setString(12, support.jsonb(chunk.metadata()));
                ps.setString(13, VectorCodec.toPgVector(chunk.embedding()));
            }

            @Override
            public int getBatchSize() {
                return chunks.size();
            }
        });
    }

    @Override
    public List<StoredChunk> findByVersion(UUID documentVersionId) {
        return jdbc.query("""
                SELECT * FROM chunks WHERE document_version_id = ? ORDER BY chunk_index
                """, chunkMapper(), documentVersionId);
    }

    @Override
    public List<ChunkSearchHit> searchCurrentVersions(String tenantId, List<AccessLevel> levels,
                                                      float[] queryEmbedding, int limit) {
        if (levels.isEmpty()) {
            return List.of();
        }
        String placeholders = String.join(",", java.util.Collections.nCopies(levels.size(), "?"));
        String vector = VectorCodec.toPgVector(queryEmbedding);

        String sql = """
                SELECT c.id, c.content, c.access_level, c.metadata,
                       d.document_key, d.title, v.version_label,
                       (c.embedding <=> CAST(? AS vector)) AS cosine_distance
                FROM chunks c
                JOIN document_versions v ON v.id = c.document_version_id
                JOIN documents d ON d.id = v.document_id AND d.current_version_id = v.id
                WHERE d.tenant_id = ?
                  AND c.access_level IN (%s)
                ORDER BY cosine_distance
                LIMIT ?
                """.formatted(placeholders);

        List<Object> ordered = new ArrayList<>();
        ordered.add(vector);
        ordered.add(tenantId);
        ordered.addAll(levels.stream().map(Enum::name).toList());
        ordered.add(limit);

        return jdbc.query(sql, (rs, i) -> new ChunkSearchHit(
                JdbcSupport.uuid(rs, "id"),
                rs.getString("document_key"),
                rs.getString("title"),
                rs.getString("version_label"),
                JdbcSupport.accessLevel(rs, "access_level"),
                rs.getString("content"),
                support.readMap(rs, "metadata"),
                rs.getDouble("cosine_distance")), ordered.toArray());
    }
}
