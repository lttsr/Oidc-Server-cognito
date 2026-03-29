package app.context.exception;

import org.springframework.security.authentication.BadCredentialsException;

import app.context.http.HttpStatusCode;
import app.context.messages.MessageUtils;

public class InvalidCredentialsException extends BadCredentialsException {
    private final String errorCode;

    public InvalidCredentialsException() {
        this(HttpStatusCode.BAD_REQUEST);
    }

    public InvalidCredentialsException(String errorCode) {
        super(resolveMessage(errorCode));
        this.errorCode = errorCode;
    }

    public InvalidCredentialsException(String errorCode, Throwable cause) {
        super(resolveMessage(errorCode), cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    private static String resolveMessage(String errorCode) {
        if (HttpStatusCode.BAD_REQUEST.equals(errorCode)) {
            return MessageUtils.getMessage("validation.error.invalid_credentials");
        }
        return MessageUtils.getMessage("error.500");
    }
}
