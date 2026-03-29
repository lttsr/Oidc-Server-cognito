package app.usecase.auth;

import java.util.List;

import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import app.context.anotation.Audit;
import app.model.userpool.UserPool;
import app.usecase.cognito.CognitoAuthService;
import app.usecase.company.CompanyLogoService;
import app.usecase.company.OAuthClientService;
import app.usecase.userpool.UserPoolService;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChallengeNameType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ForgotPasswordResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.RespondToAuthChallengeResponse;

@RequiredArgsConstructor
@Service
public class AuthEndpointService {
    private final CognitoAuthService cognitoAuthService;
    private final CompanyLogoService companyLogoService;
    private final OAuthClientService companyOauthConfigService;
    private final UserPoolService userPoolService;

    /**
     * 認可エンドポイント - 初期化
     *
     * @param savedRequest 保存済みリクエスト
     * @param clientId     クライアントID
     * @return 初期化データ
     */
    public InitResult initEndpoint(SavedRequest savedRequest, String clientId) {
        // クライアントIDを取得
        if (clientId == null || clientId.isBlank()) {
            clientId = UriComponentsBuilder.fromUriString(savedRequest.getRedirectUrl())
                    .build()
                    .getQueryParams()
                    .getFirst("client_id");
        }

        // 企業IDを取得
        var companyId = companyOauthConfigService.findCompanyIdByClientId(clientId)
                .orElse(null);

        // 企業ロゴパスを取得
        String companyLogoPath = companyLogoService.findLogoPathByCompanyId(companyId)
                .orElse(null);

        // ユーザープールリストを取得
        List<UserPool> userPoolList = userPoolService.findAllByCompanyId(companyId);

        return new InitResult(clientId, userPoolList, companyLogoPath);
    }

    @Builder
    public record InitResult(
            String clientId,
            List<UserPool> userPoolList,
            String companyLogoPath) {
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
