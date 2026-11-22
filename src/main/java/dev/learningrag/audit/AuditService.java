package dev.learningrag.audit;

import java.util.Map;

public interface AuditService {

    /**
     * Persists a structured audit event. Implementations must never store raw prompts, documents or secrets.
     */
    void record(AuditEvent event);

    record AuditEvent(
            String requestId,
            String principalId,
            String tenantId,
            String eventType,
            String promptHash,
            Integer inputTokens,
            Integer outputTokens,
            Long latencyMs,
            Map<String, Object> guardVerdicts) {

        public AuditEvent {
            guardVerdicts = guardVerdicts == null ? Map.of() : Map.copyOf(guardVerdicts);
        }
    }
}
