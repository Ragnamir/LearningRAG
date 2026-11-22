package dev.learningrag.retrieve;

import java.util.List;

import dev.learningrag.core.RequestContext;

public interface RetrievalService {

    /**
     * Returns top-k chunks of current document versions visible to the caller. Filtering happens in SQL before
     * the limit is applied, so inaccessible chunks never become candidates.
     */
    List<RetrievedChunk> retrieve(String question, RequestContext ctx, int topK);
}
