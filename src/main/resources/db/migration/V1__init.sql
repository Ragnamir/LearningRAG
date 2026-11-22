-- M0 schema: documents, immutable versions, chunks with embeddings, token budget and audit trail.

CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE documents (
    id                 UUID PRIMARY KEY,
    tenant_id          TEXT        NOT NULL,
    document_key       TEXT        NOT NULL,
    title              TEXT        NOT NULL,
    current_version_id UUID,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_documents_key UNIQUE (tenant_id, document_key)
);

CREATE TABLE document_versions (
    id            UUID PRIMARY KEY,
    document_id   UUID        NOT NULL REFERENCES documents (id) ON DELETE CASCADE,
    revision      INTEGER     NOT NULL,
    version_label TEXT        NOT NULL,
    content_hash  TEXT        NOT NULL,
    access_level  TEXT        NOT NULL,
    metadata      JSONB       NOT NULL DEFAULT '{}'::jsonb,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_version_label UNIQUE (document_id, version_label),
    CONSTRAINT uq_version_revision UNIQUE (document_id, revision),
    CONSTRAINT ck_version_access_level CHECK (access_level IN ('PUBLIC', 'INTERNAL', 'CONFIDENTIAL'))
);

ALTER TABLE documents
    ADD CONSTRAINT fk_documents_current_version
        FOREIGN KEY (current_version_id) REFERENCES document_versions (id)
        DEFERRABLE INITIALLY DEFERRED;

CREATE INDEX idx_document_versions_document ON document_versions (document_id);

CREATE TABLE chunks (
    id                  UUID PRIMARY KEY,
    document_version_id UUID        NOT NULL REFERENCES document_versions (id) ON DELETE CASCADE,
    tenant_id           TEXT        NOT NULL,
    access_level        TEXT        NOT NULL,
    chunk_index         INTEGER     NOT NULL,
    content             TEXT        NOT NULL,
    content_hash        TEXT        NOT NULL,
    start_offset        INTEGER     NOT NULL,
    end_offset          INTEGER     NOT NULL,
    paragraph_start     INTEGER     NOT NULL,
    paragraph_end       INTEGER     NOT NULL,
    metadata            JSONB       NOT NULL DEFAULT '{}'::jsonb,
    embedding           vector(384) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_chunk_index UNIQUE (document_version_id, chunk_index),
    CONSTRAINT ck_chunk_access_level CHECK (access_level IN ('PUBLIC', 'INTERNAL', 'CONFIDENTIAL')),
    CONSTRAINT ck_chunk_offsets CHECK (start_offset >= 0 AND end_offset > start_offset)
);

CREATE INDEX idx_chunks_scope ON chunks (tenant_id, access_level);
CREATE INDEX idx_chunks_version ON chunks (document_version_id);
CREATE INDEX idx_chunks_embedding_hnsw ON chunks USING hnsw (embedding vector_cosine_ops);

CREATE TABLE llm_usage (
    usage_date      DATE   PRIMARY KEY,
    reserved_tokens BIGINT NOT NULL DEFAULT 0,
    used_tokens     BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_usage_non_negative CHECK (reserved_tokens >= 0 AND used_tokens >= 0)
);

CREATE TABLE audit_events (
    id             BIGSERIAL PRIMARY KEY,
    request_id     TEXT        NOT NULL,
    principal_id   TEXT,
    tenant_id      TEXT,
    event_type     TEXT        NOT NULL,
    prompt_hash    TEXT,
    input_tokens   INTEGER,
    output_tokens  INTEGER,
    latency_ms     BIGINT,
    guard_verdicts JSONB       NOT NULL DEFAULT '{}'::jsonb,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_request ON audit_events (request_id);
CREATE INDEX idx_audit_created ON audit_events (created_at);
