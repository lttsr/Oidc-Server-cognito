package app.context.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * アプリケーションロガークラス
 */
public class AppLogger {

    /**
     * 正常処理ログを出力します。
     *
     * @param clazz   クラス
     * @param message メッセージ
     * @param params  パラメータ
     */
    public static void info(Class<?> clazz, String message, Object... params) {
        Logger logger = LoggerFactory.getLogger(clazz);
        logger.info(message, params);
    }

    /**
     * 警告ログを出力します。
     *
     * @param clazz   クラス
     * @param message メッセージ
     * @param params  パラメータ
     */
    public static void warn(Class<?> clazz, String message, Object... params) {
        Logger logger = LoggerFactory.getLogger(clazz);
        logger.warn(message, params);
    }

    /**
     * エラーログを出力します。
     *
     * @param clazz   クラス
     * @param message メッセージ
     * @param params  パラメータ
     */
    public static void error(Class<?> clazz, String message, Object... params) {
        Logger logger = LoggerFactory.getLogger(clazz);
        logger.error(message, params);
    }
}
