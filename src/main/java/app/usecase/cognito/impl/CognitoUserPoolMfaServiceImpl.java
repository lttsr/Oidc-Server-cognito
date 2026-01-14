package app.usecase.cognito.impl;

import org.springframework.stereotype.Service;

import app.context.cognito.CognitoClientFactory;
import app.context.cognito.ContextLocal;
import app.usecase.cognito.CognitoUserPoolMfaService;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserPoolMfaConfigRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserPoolMfaConfigResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SetUserPoolMfaConfigRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SetUserPoolMfaConfigResponse;

/**
 * CognitoUserPoolMfaServiceの実装クラス
 */
@RequiredArgsConstructor
@Service
public class CognitoUserPoolMfaServiceImpl implements
        CognitoUserPoolMfaService {

    private final CognitoClientFactory clientFactory;

    /**
     * GetUserPoolMfaConfig APIを使用してMFA設定を取得します。
     *
     * @return GetUserPoolMfaConfigResponse
     */
    @Override
    public GetUserPoolMfaConfigResponse getUserPoolMfaConfig() {
        var config = ContextLocal.getConfig();
        var client = clientFactory.getClient(config.getRegion());
        GetUserPoolMfaConfigRequest reqParam = GetUserPoolMfaConfigRequest.builder()
                .userPoolId(config.getUserPoolId())
                .build();
        return client.getUserPoolMfaConfig(reqParam);
    }

    /**
     * SetUserPoolMfaConfig APIを使用してMFA設定を設定します。
     *
     * @param MfaConfiguration MFA設定種別
     * @return SetUserPoolMfaConfigResponse
     */
    @Override
    public SetUserPoolMfaConfigResponse setUserPoolMfaConfig(String MfaConfiguration) {
        var config = ContextLocal.getConfig();
        var client = clientFactory.getClient(config.getRegion());
        SetUserPoolMfaConfigRequest reqParam = SetUserPoolMfaConfigRequest.builder()
                .userPoolId(config.getUserPoolId())
                .mfaConfiguration(MfaConfiguration)
                .build();
        return client.setUserPoolMfaConfig(reqParam);
    }

}
