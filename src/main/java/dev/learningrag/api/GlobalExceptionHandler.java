package dev.learningrag.api;

import dev.learningrag.core.error.ErrorCode;
import dev.learningrag.core.error.RagException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import dev.learningrag.api.dto.ErrorResponse;

/**
 * Converts every failure into a safe response: no stack traces, prompts, document text or provider payloads.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RagException.class)
    public ResponseEntity<ErrorResponse> handleRagException(RagException ex) {
        ErrorCode code = ex.code();
        if (code.status().is5xxServerError()) {
            log.warn("request failed code={}", code, ex);
        } else {
            log.info("request rejected code={} reason={}", code, ex.getMessage());
        }
        return ResponseEntity.status(code.status()).body(body(code.name(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        ErrorCode code = ErrorCode.INPUT_REJECTED;
        return ResponseEntity.status(code.status()).body(body(code.name(), "Request validation failed"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("unhandled error", ex);
        ErrorCode code = ErrorCode.INTERNAL;
        return ResponseEntity.status(code.status()).body(body(code.name(), "Internal error"));
    }

    private ErrorResponse body(String code, String message) {
        return new ErrorResponse(MDC.get(RequestIdFilter.MDC_KEY), code, message);
    }
}
