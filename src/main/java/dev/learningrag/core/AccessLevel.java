package dev.learningrag.core;

/**
 * Access level attached to a document version. A principal may read only levels it is granted.
 */
public enum AccessLevel {
    PUBLIC,
    INTERNAL,
    CONFIDENTIAL
}
