package app.context.cognito;

import app.model.userpool.UserPool;

/**
 * リクエストスコープでユーザープール設定を管理します。
 */
public class ContextLocal {

    private static final ThreadLocal<UserPool> configHolder = new ThreadLocal<>();

    /**
     * Cognito設定をセット
     */
    public static void setConfig(UserPool config) {
        configHolder.set(config);
    }

    /**
     * ユーザープール設定を取得
     */
    public static UserPool getConfig() {
        UserPool config = configHolder.get();
        if (config == null) {
            throw new IllegalStateException("CognitoConfig is not set in ContextLocal");
        }
        return config;
    }

    /**
     * コンテキストをクリア
     */
    public static void clear() {
        configHolder.remove();
    }

    /**
     * 設定がセットされているか確認
     */
    public static boolean isSet() {
        return configHolder.get() != null;
    }
}
