package app.usecase.cognito;

import software.amazon.awssdk.services.cognitoidentityprovider.model.DescribeUserPoolResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserPoolMfaConfigResponse;

public interface CognitoUserPoolService {

    /**
     * DescribeUserPool APIを使用してユーザプール情報を取得します。
     *
     * @return DescribeUserPoolResponse
     */
    DescribeUserPoolResponse describeUserPool();

    /**
     * GetUserPoolMfaConfig APIを使用してMFA設定を取得します。
     *
     * @return GetUserPoolMfaConfigResponse
     */
    GetUserPoolMfaConfigResponse getUserPoolMfaConfig();
}
