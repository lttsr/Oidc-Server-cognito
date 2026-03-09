package app.usecase.auth;

import org.springframework.stereotype.Service;

import app.context.anotation.Audit;
import app.usecase.cognito.CognitoAuthService;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final CognitoAuthService cognitoAuthService;

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
