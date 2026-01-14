package app.context.exception;

import app.context.messages.MessageUtils;

/**
 * エンコード処理関連の例外クラス
 */
public class EncodeExeption extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * エンコード処理に失敗した場合にスローされる例外
     *
     * @param cause 原因となった例外
     */
    public EncodeExeption(Throwable cause) {
        super(MessageUtils.getMessage("encode.base64"), cause);
    }

    /**
     * エンコード処理に失敗した場合にスローされる例外
     *
     * @param message エラーメッセージ
     * @param cause   原因となった例外
     */
    public EncodeExeption(String message, Throwable cause) {
        super(message, cause);
    }
}
