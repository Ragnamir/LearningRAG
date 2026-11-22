package dev.learningrag.chunk;

import java.util.List;

public interface DocumentChunker {

    /**
     * Splits normalized document text into ordered, non-empty chunks with overlap.
     */
    List<Chunk> chunk(String text);
}
