package dev.learningrag.chunk;

/**
 * A piece of a document version. Offsets refer to the normalized version text so a citation can be mapped back
 * to the original source.
 */
public record Chunk(
        int index,
        String text,
        int startOffset,
        int endOffset,
        int paragraphStart,
        int paragraphEnd) {
}
