package app.usecase.cognito;

import software.amazon.awssdk.services.cognitoidentityprovider.model.ChallengeNameType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ForgotPasswordResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.RespondToAuthChallengeResponse;

/**
 * AWS Cognito認証サービスのインターフェース
 */
public interface CognitoAuthService {

    /**
     * InitiateAuth APIを使用したユーザー認証を行います。
     *
     * @param username ユーザー名
     * @param password パスワード
     * @return InitiateAuthResponse
     */
    InitiateAuthResponse initiateAuth(String username, String password);

    /**
     * MFAチャレンジ応答
     *
     * @param session       セッションID
     * @param mfaCode       MFAコード
     * @param username      ユーザー名
     * @param challengeName 認証フロー
     * @return RespondToAuthChallengeResponse
     */
    RespondToAuthChallengeResponse respondToAuthChallenge(String session, String mfaCode, String username,
            ChallengeNameType challengeName);

    /**
     * リフレッシュトークンで新トークン取得
     *
     * @param refreshToken リフレッシュトークン
     * @return InitiateAuthResponse（REFRESH_TOKEN_AUTH の結果）
     */
    InitiateAuthResponse refreshToken(String refreshToken);

    /**
     * RevokeToken
     *
     * @param token トークン
     */
    void revokeToken(String token);

    /**
     * GlobalSignOut
     *
     * @param accessToken アクセストークン
     */
    void globalSignOut(String accessToken);

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
}
