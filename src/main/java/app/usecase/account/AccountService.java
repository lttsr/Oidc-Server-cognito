package app.usecase.account;

import java.util.Map;

import org.springframework.stereotype.Service;

import app.context.anotation.Audit;
import app.context.cognito.ValidateUserAttribute;
import app.usecase.cognito.CognitoAccountService;
import app.usecase.cognito.CognitoUserPoolService;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserResponse;

@RequiredArgsConstructor
@Service
public class AccountService {

    private final CognitoAccountService cognitoAccountService;
    private final CognitoUserPoolService cognitoUserPoolService;

    // ChangePassword APIを使用してパスワードを変更します。
    @Audit
    public void changePassword(String accessToken, String previousPassword, String proposedPassword) {
        cognitoAccountService.changePassword(accessToken, previousPassword, proposedPassword);
    }

    // GetUser APIを使用してユーザー情報を取得します。
    @Audit
    public GetUserResponse getUserInfo(String accessToken) {
        return cognitoAccountService.getUser(accessToken);
    }

    // UpdateUserAttributes APIを使用してユーザー属性を更新します。
    @Audit
    public void updateUser(String accessToken, Map<String, String> userAttributes) {
        // DescribeUserPool APIでスキーマ取得
        var schema = cognitoUserPoolService.describeUserPool();

        // スキーマを元にバリデーション
        ValidateUserAttribute.validate(schema, userAttributes);
        // Cognito API実行
        cognitoAccountService.updateUserAttributes(accessToken, userAttributes);
    }

}
