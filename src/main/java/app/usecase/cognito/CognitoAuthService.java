package app.usecase.cognito;

import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminRespondToAuthChallengeResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChallengeNameType;

/**
 * AWS Cognito認証サービスのインターフェース
 */
public interface CognitoAuthService {

    /**
     * AdminInitiateAuth
     *
     * @param username ユーザー名
     * @param password パスワード
     * @return AdminInitiateAuthResponse
     */
    AdminInitiateAuthResponse adminInitiateAuth(String username, String password);

    /**
     * VerifyMfa
     *
     * @param session       セッションID
     * @param mfaCode       MFAコード
     * @param username      ユーザー名
     * @param challengeName 認証フロー
     * @return AdminRespondToAuthChallengeResponse
     */
    AdminRespondToAuthChallengeResponse adminRespondToAuthChallenge(String session, String mfaCode, String username,
            ChallengeNameType challengeName);

    /**
     * RefreshToken
     *
     * @param refreshToken リフレッシュトークン
     * @return AdminInitiateAuthResponse
     */
    AdminInitiateAuthResponse refreshToken(String refreshToken);

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
}
