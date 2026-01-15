package app.usecase.cognito.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import app.config.aws.CognitoClientFactory;
import app.context.cognito.CalcSecretHash;
import app.context.cognito.ContextLocal;
import app.usecase.cognito.CognitoAccountService;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChangePasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ConfirmForgotPasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ForgotPasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ForgotPasswordResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UpdateUserAttributesRequest;

@RequiredArgsConstructor
@Service
public class CognitoAccountServiceImpl implements CognitoAccountService {

    private final CognitoClientFactory clientFactory;

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

    /**
     * ChangePassword API
     */
    @Override
    public void changePassword(String accessToken, String previousPassword,
            String proposedPassword) {
        var config = ContextLocal.getConfig();
        var client = clientFactory.getClient(config.getRegion());

        // ChangePasswordリクエストの構築
        ChangePasswordRequest reqParam = ChangePasswordRequest.builder()
                .accessToken(accessToken)
                .previousPassword(previousPassword)
                .proposedPassword(proposedPassword)
                .build();

        // ChangePassword API
        client.changePassword(reqParam);
    }

    /**
     * GetUser API
     */
    @Override
    public GetUserResponse getUser(String accessToken) {
        var config = ContextLocal.getConfig();
        var client = clientFactory.getClient(config.getRegion());

        // GetUserリクエストの構築
        GetUserRequest reqParam = GetUserRequest.builder()
                .accessToken(accessToken)
                .build();

        // GetUser API
        return client.getUser(reqParam);
    }

    /**
     * UpdateUserAttributes API
     */
    @Override
    public void updateUserAttributes(String accessToken, Map<String, String> userAttributes) {
        var config = ContextLocal.getConfig();
        var client = clientFactory.getClient(config.getRegion());

        // ユーザー属性の構築
        List<AttributeType> attributes = userAttributes
                .entrySet()
                .stream()
                .map(v -> AttributeType.builder()
                        .name(v.getKey())
                        .value(v.getValue())
                        .build())
                .collect(Collectors.toList());

        // UpdateUserAttributesリクエストの構築
        UpdateUserAttributesRequest reqParam = UpdateUserAttributesRequest.builder()
                .accessToken(accessToken)
                .userAttributes(attributes)
                .build();

        // UpdateUserAttributes API
        client.updateUserAttributes(reqParam);
    }
}
