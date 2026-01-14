package app.context.exception.filter;

import app.context.messages.MessageUtils;

public class UserPoolContextException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public UserPoolContextException() {
        super(MessageUtils.getMessage("userpool.error.context.invalid"));
    }

    public UserPoolContextException(String message) {
        super(message);
    }

}
