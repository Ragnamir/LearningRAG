package dev.learningrag.guard;

public interface InputGuard {

    GuardDecision check(String question);
}
