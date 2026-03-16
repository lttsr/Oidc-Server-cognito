package app.usecase.cognito.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import app.config.aws.CognitoClientFactory;
import app.context.cognito.ContextLocal;
import app.usecase.cognito.CognitoAccountService;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChangePasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UpdateUserAttributesRequest;

@RequiredArgsConstructor
@Service
public class CognitoAccountServiceImpl implements CognitoAccountService {

    private final CognitoClientFactory clientFactory;

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
