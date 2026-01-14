package app.usecase.userpool;

import org.springframework.stereotype.Service;

import app.usecase.cognito.CognitoUserPoolService;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.cognitoidentityprovider.model.DescribeUserPoolResponse;

@RequiredArgsConstructor
@Service
public class UserPoolService {

    private final CognitoUserPoolService cognitoUserPoolService;

    /**
     * DescribeUserPool APIを使用して最新のユーザプール情報を取得します。
     */
    public DescribeUserPoolResponse getUserPoolAttributes() {
        return cognitoUserPoolService.describeUserPool();
    }

}
