package app.usecase.cognito;

import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserPoolMfaConfigResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SetUserPoolMfaConfigResponse;

public interface CognitoUserPoolMfaService {
    /**
     * GetUserPoolMfaConfig APIを使用してMFA設定を取得します。
     *
     * @return GetUserPoolMfaConfigResponse
     */
    GetUserPoolMfaConfigResponse getUserPoolMfaConfig();

    /**
     * SetUserPoolMfaConfig APIを使用してMFAの設定を変更します。
     *
     * @param MfaConfiguration MFA設定種別 (必須 | 無効 | オプション)
     * @return SetUserPoolMfaConfigResponse
     */
    SetUserPoolMfaConfigResponse setUserPoolMfaConfig(String MfaConfiguration);

}
