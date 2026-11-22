package dev.learningrag.auth;

import jakarta.servlet.http.HttpServletRequest;

import dev.learningrag.core.RequestContext;

public interface AuthenticationService {

    /**
     * Authenticates the caller and returns the immutable security context. Throws on failure; never returns a
     * permissive default.
     */
    RequestContext authenticate(HttpServletRequest request);
}
