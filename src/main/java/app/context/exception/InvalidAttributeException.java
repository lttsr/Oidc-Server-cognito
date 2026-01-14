package app.context.exception;

import java.util.Map;

public class InvalidAttributeException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final Map<String, String> errors;

    public InvalidAttributeException(Map<String, String> errors) {
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
