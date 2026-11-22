package dev.learningrag.prompt;

import java.util.List;

import dev.learningrag.retrieve.RetrievedChunk;

public interface PromptBuilder {

    Prompt build(String question, List<RetrievedChunk> chunks);
}
