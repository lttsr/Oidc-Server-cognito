package app.usecase.cognito;

import java.util.Map;

import software.amazon.awssdk.services.cognitoidentityprovider.model.ForgotPasswordResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserResponse;

/**
 * AWS Cognitoアカウントサービスのインターフェース
 */

public interface CognitoAccountService {
    /**
     * ForgotPassword
     *
     * @param userName ユーザー名
     * @return ForgotPasswordResponse
     */
    ForgotPasswordResponse forgotPassword(String userName);

    /**
     * ConfirmForgotPassword
     *
     * @param userName         ユーザー名
     * @param confirmationCode 認証コード
     * @param password         パスワード
     */
    void confirmForgotPassword(String userName, String confirmationCode, String password);

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
