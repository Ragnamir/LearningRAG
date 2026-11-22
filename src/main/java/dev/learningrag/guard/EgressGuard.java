package dev.learningrag.guard;

import java.util.List;

import dev.learningrag.core.Message;

/**
 * Inspects everything that would leave the process. Fails closed on any match or internal error.
 */
public interface EgressGuard {

    GuardDecision inspect(List<Message> messages, String serializedBody);
}
