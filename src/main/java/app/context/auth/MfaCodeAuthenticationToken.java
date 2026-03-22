package app.context.auth;

import java.util.List;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/**
 * MFAコード検証リクエストを表すトークンです。
 * /api/auth/verify-mfa で受けた入力値を Provider へ渡す用途で使用します。
 */
public class MfaCodeAuthenticationToken extends AbstractAuthenticationToken {
    private final String sessionId;
    private final String mfaCode;

    public MfaCodeAuthenticationToken(String sessionId, String mfaCode) {
        super(List.<GrantedAuthority>of());
        this.sessionId = sessionId;
        this.mfaCode = mfaCode;
        setAuthenticated(false);
    }

    @Override
    public Object getCredentials() {
        return mfaCode;
    }

    @Override
    public Object getPrincipal() {
        return sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }
}
