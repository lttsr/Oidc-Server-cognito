package app.usecase.cognito;

import java.util.Map;

import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserResponse;

/**
 * AWS Cognitoアカウントサービスのインターフェース
 */

public interface CognitoAccountService {

    /**
     * ChangePassword
     *
     * @param accessToken      アクセストークン
     * @param previousPassword 旧パスワード
     * @param proposedPassword 新パスワード
     */
    void changePassword(String accessToken, String previousPassword, String proposedPassword);

    /**
     * GetUser
     *
     * @param accessToken アクセストークン
     * @return GetUserResponse
     */
    GetUserResponse getUser(String accessToken);

    /**
     * UpdateUserAttributes
     *
     * @param accessToken    アクセストークン
     * @param userAttributes ユーザー属性
     */
    void updateUserAttributes(String accessToken, Map<String, String> userAttributes);
}
