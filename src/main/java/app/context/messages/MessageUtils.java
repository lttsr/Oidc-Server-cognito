package app.context.messages;

import java.util.Locale;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
public class MessageUtils implements ApplicationContextAware {

    private static MessageSource messageSource;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        messageSource = applicationContext.getBean(MessageSource.class);
    }

    /**
     * メッセージを取得
     *
     * @param key メッセージキー
     * @return メッセージ
     */
    public static String getMessage(String key) {
        return getMessage(key, null, key);
    }

    /**
     * メッセージを取得（パラメータ付）
     *
     * @param key  メッセージキー
     * @param args パラメータ（{0}, {1}, {min}, {max} など）
     * @return メッセージ
     */
    public static String getMessage(String key, Object[] args) {
        return getMessage(key, args, key);
    }

    /**
     * メッセージを取得（デフォルトメッセージ付）
     *
     * @param key            メッセージキー
     * @param args           パラメータ
     * @param defaultMessage デフォルトメッセージ（キーが見つからない場合）
     * @return メッセージ
     */
    public static String getMessage(String key, Object[] args, String defaultMessage) {
        try {
            return messageSource.getMessage(key, args, Locale.getDefault());
        } catch (Exception e) {
            return defaultMessage;
        }
    }

    /**
     * バリデーションメッセージを取得
     *
     * @param fieldName      フィールド名
     * @param validationCode バリデーションコード（NotBlank, Size など）
     * @param args           パラメータ
     * @param defaultMessage デフォルトメッセージ
     * @return メッセージ
     */
    public static String getValidationMessage(
            String fieldName,
            String validationCode,
            Object[] args,
            String defaultMessage) {
        String key = String.format("validation.%s.%s", fieldName, validationCode);
        return getMessage(key, args, defaultMessage);
    }
}
