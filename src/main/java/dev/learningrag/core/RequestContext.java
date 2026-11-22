package dev.learningrag.core;

import java.time.Instant;
import java.util.Set;

/**
 * Per-request security context. Tenant and allowed access levels are fixed at authentication time and
 * are never derived from user-supplied request fields.
 */
public record RequestContext(
        String requestId,
        String principalId,
        String tenantId,
        Set<AccessLevel> allowedLevels,
        Instant receivedAt) {

    public RequestContext {
        allowedLevels = Set.copyOf(allowedLevels);
    }

    public boolean canRead(AccessLevel level) {
        return allowedLevels.contains(level);
    }
}
