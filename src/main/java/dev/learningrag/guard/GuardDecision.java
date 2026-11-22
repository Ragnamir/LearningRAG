package dev.learningrag.guard;

import java.util.Map;

/**
 * Explicit outcome of a guard. Guards fail closed: anything other than {@link #allowed()} stops the pipeline.
 */
public record GuardDecision(boolean allowed, String reasonCode, Map<String, Object> details) {

    public GuardDecision {
        details = details == null ? Map.of() : Map.copyOf(details);
    }

    public static GuardDecision allow() {
        return new GuardDecision(true, "ALLOW", Map.of());
    }

    public static GuardDecision allow(Map<String, Object> details) {
        return new GuardDecision(true, "ALLOW", details);
    }

    public static GuardDecision deny(String reasonCode) {
        return new GuardDecision(false, reasonCode, Map.of());
    }

    public static GuardDecision deny(String reasonCode, Map<String, Object> details) {
        return new GuardDecision(false, reasonCode, details);
    }
}
