package app.context.auth;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CognitoAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final SavedRequestAwareAuthenticationSuccessHandler delegate = new SavedRequestAwareAuthenticationSuccessHandler();

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        if (authentication instanceof MfaPendingAuthenticationToken mfaPending) {
            SecurityContextHolder.clearContext();
            response.sendRedirect("/api/auth/mfa?sessionId=" + mfaPending.getSessionId());
            return;
        }

        delegate.onAuthenticationSuccess(request, response, authentication);
    }
}
