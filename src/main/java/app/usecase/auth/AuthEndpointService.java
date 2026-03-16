package app.usecase.auth;

import java.util.List;

import org.springframework.stereotype.Service;

import app.context.anotation.Audit;
import app.model.userpool.UserPool;
import app.usecase.cognito.CognitoAuthService;
import app.usecase.company.CompanyService;
import app.usecase.userpool.UserPoolService;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminRespondToAuthChallengeResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChallengeNameType;

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
    // AdminInitiateAuth APIを使用した利用者認証を行います。
    @Audit
    public AdminInitiateAuthResponse authUser(String userName, String password) {

        return cognitoAuthService.adminInitiateAuth(userName, password);

    }

    // RespondToAuthChallenge APIを使用したMFA認証を行います。
    @Audit
    public AdminRespondToAuthChallengeResponse verifyMfaUser(String session, String mfaCode, String userName,
            ChallengeNameType challengeName) {
        return cognitoAuthService.adminRespondToAuthChallenge(session, mfaCode, userName, challengeName);
    }

    // RefreshToken APIを使用して既に発行されているトークンをリフレッシュします。
    @Audit
    public AdminInitiateAuthResponse refreshToken(String refreshToken) {
        return cognitoAuthService.refreshToken(refreshToken);
    }

}
