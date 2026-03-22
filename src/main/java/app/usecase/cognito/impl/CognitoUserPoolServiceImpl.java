package app.usecase.cognito.impl;

import org.springframework.stereotype.Service;

import app.context.cognito.CognitoClientFactory;
import app.context.cognito.ContextLocal;
import app.usecase.cognito.CognitoUserPoolService;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.cognitoidentityprovider.model.DescribeUserPoolRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.DescribeUserPoolResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserPoolMfaConfigRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserPoolMfaConfigResponse;

@RequiredArgsConstructor
@Service
public class CognitoUserPoolServiceImpl implements CognitoUserPoolService {

    private final CognitoClientFactory clientFactory;

    // DescribeUserPool APIを使用してユーザプール情報を取得します。
    @Override
    public DescribeUserPoolResponse describeUserPool() {
        var config = ContextLocal.getConfig();
        var client = clientFactory.getClient(config.getRegion());
        DescribeUserPoolRequest reqParam = DescribeUserPoolRequest.builder()
                .userPoolId(config.getUserPoolId())
                .build();
        return client.describeUserPool(reqParam);
    }

    // GetUserPoolMfaConfig APIを使用してMFA設定を取得します。
    @Override
    public GetUserPoolMfaConfigResponse getUserPoolMfaConfig() {
        var config = ContextLocal.getConfig();
        var client = clientFactory.getClient(config.getRegion());
        GetUserPoolMfaConfigRequest reqParam = GetUserPoolMfaConfigRequest.builder()
                .userPoolId(config.getUserPoolId())
                .build();
        return client.getUserPoolMfaConfig(reqParam);
    }
}
