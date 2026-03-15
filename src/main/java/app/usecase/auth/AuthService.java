package app.usecase.auth;

import org.springframework.stereotype.Service;

import app.context.anotation.Audit;
import app.usecase.cognito.CognitoAuthService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final CognitoAuthService cognitoAuthService;

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
