package dev.learningrag.api.dto;

/**
 * Citation of a retrieved chunk. Only chunks actually passed to the model in this request may be cited.
 */
public record CitationDto(
        String chunkId,
        String documentKey,
        String versionLabel,
        String title) {
}
