package dev.learningrag.store;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.learningrag.core.AccessLevel;

public final class JdbcSupport {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    public JdbcSupport(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String jsonb(Map<String, Object> value) throws SQLException {
        try {
            return objectMapper.writeValueAsString(value == null ? Map.of() : value);
        } catch (Exception e) {
            throw new SQLException("Cannot serialize metadata", e);
        }
    }

    public Map<String, Object> readMap(ResultSet rs, String column) throws SQLException {
        String raw = rs.getString(column);
        if (raw == null || raw.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(raw, MAP_TYPE);
        } catch (Exception e) {
            throw new SQLException("Cannot parse jsonb column " + column, e);
        }
    }

    public static UUID uuid(ResultSet rs, String column) throws SQLException {
        return rs.getObject(column, UUID.class);
    }

    public static AccessLevel accessLevel(ResultSet rs, String column) throws SQLException {
        return AccessLevel.valueOf(rs.getString(column));
    }

    public static java.time.Instant instant(ResultSet rs, String column) throws SQLException {
        OffsetDateTime value = rs.getObject(column, OffsetDateTime.class);
        return value == null ? null : value.toInstant();
    }
}
