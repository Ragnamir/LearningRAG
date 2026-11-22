package dev.learningrag.core;

import dev.learningrag.api.dto.ChatResponse;

public interface RagPipeline {

    ChatResponse ask(String question, RequestContext ctx);
}
