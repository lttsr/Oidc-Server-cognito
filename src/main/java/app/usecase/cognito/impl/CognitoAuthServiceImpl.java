package app.usecase.cognito.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import app.config.aws.CognitoClientFactory;
import app.context.cognito.CalcSecretHash;
import app.context.cognito.ContextLocal;
import app.usecase.cognito.CognitoAuthService;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChallengeNameType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ConfirmForgotPasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ForgotPasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ForgotPasswordResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GlobalSignOutRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
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

    // InitiateAuth APIを使用したユーザー認証を行います。
    @Override
    public InitiateAuthResponse initiateAuth(String username, String password) {
        var config = ContextLocal.getConfig();
        var client = clientFactory.getClient(config.getRegion());

        Map<String, String> authParameters = new HashMap<>();
        authParameters.put("USERNAME", username);
        authParameters.put("PASSWORD", password);

        if (config.getClientSecret() != null &&
                !config.getClientSecret().isEmpty()) {
            String secretHash = CalcSecretHash.generateSecretHash(
                    username,
                    config.getClientId(),
                    config.getClientSecret());
            authParameters.put("SECRET_HASH", secretHash);
        }

        InitiateAuthRequest reqParam = InitiateAuthRequest.builder()
                .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                .clientId(config.getClientId())
                .authParameters(authParameters)
                .build();

        return client.initiateAuth(reqParam);
    }

    // RespondToAuthChallenge で MFA 認証を行います。
    @Override
    public RespondToAuthChallengeResponse respondToAuthChallenge(String session,
            String mfaCode, String username,
            ChallengeNameType challengeName) {
        var config = ContextLocal.getConfig();
        var client = clientFactory.getClient(config.getRegion());

        Map<String, String> challengeResponses = new HashMap<>();
        challengeResponses.put("USERNAME", username);

        if (challengeName == ChallengeNameType.SMS_MFA) {
            challengeResponses.put("SMS_MFA_CODE", mfaCode);
        } else if (challengeName == ChallengeNameType.SOFTWARE_TOKEN_MFA) {
            challengeResponses.put("SOFTWARE_TOKEN_MFA_CODE", mfaCode);
        }

        if (config.getClientSecret() != null &&
                !config.getClientSecret().isEmpty()) {
            String secretHash = CalcSecretHash.generateSecretHash(
                    username,
                    config.getClientId(),
                    config.getClientSecret());
            challengeResponses.put("SECRET_HASH", secretHash);
        }

        RespondToAuthChallengeRequest reqParam = RespondToAuthChallengeRequest.builder()
                .challengeName(challengeName)
                .session(session)
                .clientId(config.getClientId())
                .challengeResponses(challengeResponses)
                .build();

        return client.respondToAuthChallenge(reqParam);
    }

    /**
     * リフレッシュトークンで新トークン取得
     */
    @Override
    public InitiateAuthResponse refreshToken(String refreshToken) {
        var config = ContextLocal.getConfig();
        var client = clientFactory.getClient(config.getRegion());

        Map<String, String> authParameters = new HashMap<>();
        authParameters.put("REFRESH_TOKEN", refreshToken);

        if (config.getClientSecret() != null &&
                !config.getClientSecret().isEmpty()) {
            String secretHash = CalcSecretHash.generateSecretHash(
                    "",
                    config.getClientId(),
                    config.getClientSecret());
            authParameters.put("SECRET_HASH", secretHash);
        }

        InitiateAuthRequest reqParam = InitiateAuthRequest.builder()
                .authFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
                .clientId(config.getClientId())
                .authParameters(authParameters)
                .build();

        return client.initiateAuth(reqParam);
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

    /**
     * ForgotPassword API
     */
    @Override
    public ForgotPasswordResponse forgotPassword(String userName) {
        var config = ContextLocal.getConfig();
        var client = clientFactory.getClient(config.getRegion());

        // ForgotPasswordリクエストの構築
        ForgotPasswordRequest.Builder requestBuilder = ForgotPasswordRequest.builder()
                .clientId(config.getClientId())
                .username(userName);

        // clientSecretが設定されている場合のみ追加
        if (config.getClientSecret() != null && !config.getClientSecret().isEmpty()) {
            requestBuilder.secretHash(
                    CalcSecretHash.generateSecretHash(
                            userName,
                            config.getClientId(),
                            config.getClientSecret()));
        }

        // ForgotPassword API
        return client.forgotPassword(requestBuilder.build());
    }

    /**
     * ConfirmForgotPassword API
     */
    @Override
    public void confirmForgotPassword(String userName, String confirmationCode,
            String password) {
        var config = ContextLocal.getConfig();
        var client = clientFactory.getClient(config.getRegion());

        // ConfirmForgotPasswordリクエストの構築
        ConfirmForgotPasswordRequest.Builder requestBuilder = ConfirmForgotPasswordRequest.builder()
                .clientId(config.getClientId())
                .username(userName)
                .confirmationCode(confirmationCode)
                .password(password);

        // clientSecretが設定されている場合のみ追加
        if (config.getClientSecret() != null && !config.getClientSecret().isEmpty()) {
            requestBuilder.secretHash(CalcSecretHash.generateSecretHash(
                    userName,
                    config.getClientId(),
                    config.getClientSecret()));
        }

        // ConfirmForgotPassword API
        client.confirmForgotPassword(requestBuilder.build());
    }

}
