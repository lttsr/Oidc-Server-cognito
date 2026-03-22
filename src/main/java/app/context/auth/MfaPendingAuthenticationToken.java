package app.context.auth;

import java.util.List;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/**
 * パスワード認証は成功したが、MFA完了待ちであることを表すトークンです。
 * SuccessHandler でこの型を検知し、MFA画面へ遷移させます。
 */
public class MfaPendingAuthenticationToken extends AbstractAuthenticationToken {
    private final String principal;
    private final String sessionId;

    public MfaPendingAuthenticationToken(String principal, String sessionId) {
        super(List.<GrantedAuthority>of());
        this.principal = principal;
        this.sessionId = sessionId;
        setAuthenticated(false);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    public String getSessionId() {
        return sessionId;
    }
}
