package app.config.filter;

import java.io.IOException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import app.context.exception.filter.VerificationApiKeyException;
import app.context.messages.MessageUtils;
import app.usecase.cliant.CliantKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ApiKeyVerificationFilter extends OncePerRequestFilter {

    private final CliantKeyService cliantKeyService;
    private final PasswordEncoder passwordEncoder;

    private static final RequestMatcher API_AUTH_MATCHER = new AntPathRequestMatcher("/api/auth/**");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (!API_AUTH_MATCHER.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String cliantIdStr = request.getHeader("X-Cliant-Id");
            String secretKey = request.getHeader("X-Secret-Key");

            if (cliantIdStr == null || secretKey == null || secretKey.isEmpty()) {
                throw new VerificationApiKeyException(
                        MessageUtils.getMessage("verification.error.required"));
            }

            Long cliantId = Long.parseLong(cliantIdStr);

            // 企業IDからAPIキーを取得
            var cliantKeyOpt = cliantKeyService.getCliantKey(cliantId);

            // APIキーの認証
            if (cliantKeyOpt.isEmpty() || !cliantKeyOpt.get().isValid() ||
                    !passwordEncoder.matches(secretKey, cliantKeyOpt.get().getSecretKeyHash())) {
                throw new VerificationApiKeyException(MessageUtils.getMessage("verification.error.not_verified"));
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            throw new VerificationApiKeyException();
        }
    }
}
