package app.usecase.userpool;

import org.springframework.stereotype.Service;

import app.controller.userpool.type.MfaConfigType;
import app.usecase.cognito.CognitoUserPoolMfaService;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserPoolMfaConfigResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SetUserPoolMfaConfigResponse;

/**
 * ユーザプールMFA設定サービス
 */
@RequiredArgsConstructor
@Service
public class UserPoolMfaService {
    private final CognitoUserPoolMfaService cognitoUserPoolMfaService;

    /**
     * MFAの設定を取得します。
     *
     * @return GetUserPoolMfaConfigResponse
     */
    public GetUserPoolMfaConfigResponse getMfaConfig() {
        return cognitoUserPoolMfaService.getUserPoolMfaConfig();
    }

    /**
     * MFAの設定を変更します。
     *
     * @param configType MFA設定種別 (必須 | 無効 | オプション)
     * @return SetUserPoolMfaConfigResponse
     */
    public SetUserPoolMfaConfigResponse setMfaConfig(MfaConfigType configType) {
        return cognitoUserPoolMfaService.setUserPoolMfaConfig(configType.name());
    }

}
