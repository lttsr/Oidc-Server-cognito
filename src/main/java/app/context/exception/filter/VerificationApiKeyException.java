package app.context.exception.filter;

import app.context.messages.MessageUtils;

/**
 * APIキー認証失敗時の例外クラス
 */
public class VerificationApiKeyException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public VerificationApiKeyException() {
        super(MessageUtils.getMessage("verification.invalid"));
    }

    public VerificationApiKeyException(String message) {
        super(message);
    }
}
