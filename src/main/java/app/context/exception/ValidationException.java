package app.context.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.core.MethodParameter;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import app.context.messages.MessageUtils;

/**
 * バリデーションエラーが発生した場合の例外クラス
 */
public class ValidationException extends MethodArgumentNotValidException {
    private static final long serialVersionUID = 1L;
    private final Map<String, String> errors = new HashMap<>();

    public ValidationException(MethodParameter params, BindingResult bindingResult) {
        super(params, bindingResult);
        // 全フィールドのエラー情報を格納
        getBindingResult().getFieldErrors()
                .forEach(err -> errors.put(err.getField(),
                        MessageUtils.getValidationMessage(
                                err.getField(),
                                err.getCode(),
                                err.getArguments(),
                                err.getDefaultMessage() != null ? err.getDefaultMessage() : err.getCode())));
    }

    /**
     * エラーメッセージを取得
     *
     * @return エラーメッセージ
     */
    public Map<String, String> getErrors() {
        return errors;
    }
}
