package dev.learningrag.core.error;

public class RagException extends RuntimeException {

    private final ErrorCode code;

    public RagException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public RagException(ErrorCode code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public ErrorCode code() {
        return code;
    }
}
