package app.config.filter;

import java.io.IOException;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import app.context.auth.CognitoAuthenticationSuccessHandler;
import app.context.auth.MfaCodeAuthenticationToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MfaCodeAuthenticationFilter extends OncePerRequestFilter {
    private static final String MFA_VERIFY_PATH = "/api/auth/verify-mfa";

    private final AuthenticationManager authenticationManager;
    private final CognitoAuthenticationSuccessHandler authenticationSuccessHandler;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        if (!MFA_VERIFY_PATH.equals(request.getServletPath())
                || !"POST".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String sessionId = request.getParameter("sessionId");
        String mfaCode = request.getParameter("mfaCode");

        try {
            Authentication mfaToken = new MfaCodeAuthenticationToken(sessionId, mfaCode);
            Authentication authenticated = authenticationManager.authenticate(mfaToken);
            authenticationSuccessHandler.onAuthenticationSuccess(request, response, authenticated);
        } catch (AuthenticationException e) {
            response.sendRedirect("/api/auth/mfa?sessionId=" + sessionId + "&error");
        }
    }
}
