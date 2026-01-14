package app.usecase.auth;

import org.springframework.stereotype.Service;

import app.context.anotation.Audit;
import app.usecase.cognito.CognitoAuthService;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChallengeNameType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.RespondToAuthChallengeResponse;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final CognitoAuthService cognitoAuthService;

    // AdminInitiateAuth APIを使用した利用者認証を行います。
    @Audit
    public AdminInitiateAuthResponse authUser(String userName, String password) {
        return cognitoAuthService.adminInitiateAuth(userName, password);

    }

    // RespondToAuthChallenge APIを使用したMFA認証を行います。
    @Audit
    public RespondToAuthChallengeResponse verifyMfaUser(String session, String mfaCode, String userName,
            ChallengeNameType challengeName) {
        return cognitoAuthService.respondToAuthChallenge(session, mfaCode, userName, challengeName);
    }

    // RefreshToken APIを使用して既に発行されているトークンをリフレッシュします。
    @Audit
    public AdminInitiateAuthResponse refreshToken(String refreshToken) {
        return cognitoAuthService.refreshToken(refreshToken);
    }

    // RevokeToken APIを使用してトークンを無効化します。
    @Audit
    public void logout(String token) {
        cognitoAuthService.revokeToken(token);
    }

    // GlobalSignOut APIを使用してトークンを無効化します。
    @Audit
    public void globalLogout(String accessToken) {
        cognitoAuthService.globalSignOut(accessToken);
    }

}
