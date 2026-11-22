package dev.learningrag.llm;

import java.util.List;

import dev.learningrag.core.LlmResponse;
import dev.learningrag.core.Message;
import dev.learningrag.core.RequestContext;

/**
 * Single entry point to the model provider. Implementations must apply egress scanning, budget accounting,
 * retries and audit themselves so that no caller can bypass them.
 */
public interface LlmGateway {

    LlmResponse chat(List<Message> messages, RequestContext ctx);
}
