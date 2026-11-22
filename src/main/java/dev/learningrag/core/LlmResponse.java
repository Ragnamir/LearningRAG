package dev.learningrag.core;

/**
 * Raw result of a single model call. Usage figures come from the provider response and drive budget accounting.
 */
public record LlmResponse(
        String content,
        int inputTokens,
        int outputTokens,
        String model,
        long latencyMillis) {

    public int totalTokens() {
        return inputTokens + outputTokens;
    }
}
