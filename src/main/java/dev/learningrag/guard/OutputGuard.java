package dev.learningrag.guard;

import java.util.List;
import java.util.Set;

import dev.learningrag.retrieve.RetrievedChunk;

public interface OutputGuard {

    GuardDecision inspect(String answer, Set<String> allowedChunkIds, List<RetrievedChunk> chunks);
}
