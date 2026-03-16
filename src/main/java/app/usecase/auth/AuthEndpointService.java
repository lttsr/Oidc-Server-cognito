package app.usecase.auth;

import java.util.List;

import org.springframework.stereotype.Service;

import app.context.anotation.Audit;
import app.model.userpool.UserPool;
import app.usecase.cognito.CognitoAuthService;
import app.usecase.company.CompanyService;
import app.usecase.userpool.UserPoolService;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChallengeNameType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ForgotPasswordResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.RespondToAuthChallengeResponse;

@RequiredArgsConstructor
@Service
public class AuthEndpointService {
    private final CognitoAuthService cognitoAuthService;
    private final CompanyService companyService;
    private final UserPoolService userPoolService;

    /**
     * 認可エンドポイント - 初期化
     *
     * @param companyId 企業ID
     * @return ユーザープール一覧
     */
    public List<UserPool> initEndpoint(Long companyId) {
        var company = companyService.findCompanyById(companyId);
        if (company.isEmpty()) {
            // TODO: 企業情報エラー
        }

        // ユーザープール一覧を取得
        return userPoolService.findAllByCompanyId(companyId);
    }

    /**
     * 認可エンドポイント - ログイン
     *
     * @param sessionId セッションID
     * @param username  ユーザー名
     * @param password  パスワード
     * @return ログイン結果
     */
    /** InitiateAuth（USER_PASSWORD_AUTH）でユーザー認証を行います。 */
    @Audit
    public InitiateAuthResponse authUser(String userName, String password) {
        return cognitoAuthService.initiateAuth(userName, password);
    }

    /** RespondToAuthChallenge で MFA 認証を行います。 */
    @Audit
    public RespondToAuthChallengeResponse verifyMfaUser(String session, String mfaCode, String userName,
            ChallengeNameType challengeName) {
        return cognitoAuthService.respondToAuthChallenge(session, mfaCode, userName, challengeName);
    }

    /** リフレッシュトークンで新トークンを取得します。 */
    @Audit
    public InitiateAuthResponse refreshToken(String refreshToken) {
        return cognitoAuthService.refreshToken(refreshToken);
    }

    // ForgotPassword APIを使用してパスワードリセットを実行します。
    @Audit
    public ForgotPasswordResponse resetPassword(String userName) {
        return cognitoAuthService.forgotPassword(userName);
    }

    // ConfirmForgotPassword APIを使用してパスワードリセットを確認します。
    @Audit
    public void confirmResetPassword(String userName, String confirmationCode, String password) {
        cognitoAuthService.confirmForgotPassword(userName, confirmationCode, password);
    }

}
