package app.usecase.cognito.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import app.config.aws.CognitoClientFactory;
import app.context.cognito.CalcSecretHash;
import app.context.cognito.ContextLocal;
import app.usecase.cognito.CognitoAuthService;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChallengeNameType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GlobalSignOutRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.RespondToAuthChallengeRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.RespondToAuthChallengeResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.RevokeTokenRequest;

/**
 * AWS Cognito認証サービスの実装クラス
 */
@RequiredArgsConstructor
@Service
public class CognitoAuthServiceImpl implements CognitoAuthService {

    private final CognitoClientFactory clientFactory;

    // AdminInitiateAuth APIを使用した利用者認証を行います。
    @Override
    public AdminInitiateAuthResponse adminInitiateAuth(String username, String password) {
        var config = ContextLocal.getConfig();
        var client = clientFactory.getClient(config.getRegion());

        // 認証パラメータの構築
        Map<String, String> authParameters = new HashMap<>();
        authParameters.put("USERNAME", username);
        authParameters.put("PASSWORD", password);

        // clientSecretが設定されている場合のみSECRET_HASHを追加
        if (config.getClientSecret() != null &&
                !config.getClientSecret().isEmpty()) {
            // SECRET_HASHを生成
            String secretHash = CalcSecretHash.generateSecretHash(
                    username,
                    config.getClientId(),
                    config.getClientSecret());
            authParameters.put("SECRET_HASH", secretHash);
        }

        // AdminInitiateAuthリクエストの構築
        AdminInitiateAuthRequest reqParam = AdminInitiateAuthRequest.builder()
                .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                .clientId(config.getClientId())
                .userPoolId(config.getUserPoolId())
                .authParameters(authParameters)
                .build();

        // Cognito認証の実行
        return client.adminInitiateAuth(reqParam);
    }

    // RespondToAuthChallenge APIを使用したMFA認証を行います。
    @Override
    public RespondToAuthChallengeResponse respondToAuthChallenge(String session,
            String mfaCode, String username,
            ChallengeNameType challengeName) {
        var config = ContextLocal.getConfig();
        var client = clientFactory.getClient(config.getRegion());

        // チャレンジレスポンスの構築
        Map<String, String> challengeResponses = new HashMap<>();
        challengeResponses.put("USERNAME", username);

        if (challengeName == ChallengeNameType.SMS_MFA) {
            challengeResponses.put("SMS_MFA_CODE", mfaCode); // SMSの場合
        } else if (challengeName == ChallengeNameType.SOFTWARE_TOKEN_MFA) {
            challengeResponses.put("SOFTWARE_TOKEN_MFA_CODE", mfaCode); // TOTPの場合
        }

        // SECRET_HASHが必要な場合
        if (config.getClientSecret() != null &&
                !config.getClientSecret().isEmpty()) {
            String secretHash = CalcSecretHash.generateSecretHash(
                    username,
                    config.getClientId(),
                    config.getClientSecret());
            challengeResponses.put("SECRET_HASH", secretHash);
        }

        // RespondToAuthChallengeリクエストの構築
        RespondToAuthChallengeRequest reqParam = RespondToAuthChallengeRequest.builder()
                .challengeName(challengeName)
                .session(session)
                .clientId(config.getClientId())
                .challengeResponses(challengeResponses)
                .build();

        // MFA認証実行
        return client.respondToAuthChallenge(reqParam);
    }

    /**
     * RefreshToken API
     */
    @Override
    public AdminInitiateAuthResponse refreshToken(String refreshToken) {
        var config = ContextLocal.getConfig();
        var client = clientFactory.getClient(config.getRegion());

        // 認証パラメータの構築
        Map<String, String> authParameters = new HashMap<>();
        authParameters.put("REFRESH_TOKEN", refreshToken);

        // clientSecretが設定されている場合のみSECRET_HASHを追加
        if (config.getClientSecret() != null &&
                !config.getClientSecret().isEmpty()) {
            // リフレッシュトークンの場合、ユーザー名が不要なため空文字を使用
            String secretHash = CalcSecretHash.generateSecretHash(
                    "",
                    config.getClientId(),
                    config.getClientSecret());
            authParameters.put("SECRET_HASH", secretHash);
        }

        // AdminInitiateAuthリクエストの構築
        AdminInitiateAuthRequest reqParam = AdminInitiateAuthRequest.builder()
                .authFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
                .clientId(config.getClientId())
                .userPoolId(config.getUserPoolId())
                .authParameters(authParameters)
                .build();

        // Cognito認証実行
        return client.adminInitiateAuth(reqParam);
    }

    /**
     * RevokeToken API
     */
    @Override
    public void revokeToken(String token) {
        var config = ContextLocal.getConfig();
        var client = clientFactory.getClient(config.getRegion());

        // RevokeTokenリクエストの構築
        RevokeTokenRequest.Builder requestBuilder = RevokeTokenRequest.builder()
                .token(token)
                .clientId(config.getClientId());

        // clientSecretが設定されている場合のみ追加
        if (config.getClientSecret() != null &&
                !config.getClientSecret().isEmpty()) {
            requestBuilder.clientSecret(config.getClientSecret());
        }

        // トークン無効化の実行
        client.revokeToken(requestBuilder.build());
    }

    /**
     * GlobalSignOut API
     */
    @Override
    public void globalSignOut(String accessToken) {
        var config = ContextLocal.getConfig();
        var client = clientFactory.getClient(config.getRegion());

        // GlobalSignOutリクエストの構築
        GlobalSignOutRequest reqParam = GlobalSignOutRequest.builder()
                .accessToken(accessToken)
                .build();

        // 全デバイスのトークン無効化の実行
        client.globalSignOut(reqParam);
    }
}
