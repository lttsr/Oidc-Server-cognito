package app.context.exception.filter;

import app.context.messages.MessageUtils;

/**
 * レート制限を超えた場合の例外クラス
 */
public class TooManyRequestsException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TooManyRequestsException() {
        super(MessageUtils.getMessage("rate.limit.exceeded"));
    }
}
