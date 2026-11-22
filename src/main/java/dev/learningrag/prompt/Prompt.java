package dev.learningrag.prompt;

import java.util.List;
import java.util.Set;

import dev.learningrag.core.Message;
import dev.learningrag.retrieve.RetrievedChunk;

/**
 * Built prompt plus the exact set of chunk ids that were handed to the model. Citation validation and output
 * guards use this set as the allow-list.
 */
public record Prompt(List<Message> messages, Set<String> allowedChunkIds, List<RetrievedChunk> chunks) {

    public Prompt {
        allowedChunkIds = Set.copyOf(allowedChunkIds);
        chunks = List.copyOf(chunks);
    }
}
