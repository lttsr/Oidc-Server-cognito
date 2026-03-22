package app.config.filter;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import app.context.cognito.ContextLocal;
import app.model.userpool.UserPool;
import app.usecase.userpool.UserPoolService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/*
 * JWT検証フィルター
 * JWTトークンの検証を行います。
 * <p>
 * 検証に成功した場合、ContextLocalにユーザープール情報をセットし、SecurityContextにJwtAuthenticationTokenをセットします。
 */
@Component
@RequiredArgsConstructor
public class JwtVerificationFilter extends OncePerRequestFilter {

    private final BearerTokenResolver bearerTokenResolver = new DefaultBearerTokenResolver();
    private final UserPoolService userPoolService;
    private final JwtDecoder selfJwtDecoder;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // ログインエンドポイントは除外
        if (new AntPathRequestMatcher("/api/auth/**").matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = bearerTokenResolver.resolve(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Jwt jwt = selfJwtDecoder.decode(token);
            String userPoolId = jwt.getClaimAsString("user_pool_id");
            UserPool userPool = userPoolService.findByUserPoolId(userPoolId);
            ContextLocal.setConfig(userPool);

            // JWT署名検証
            Authentication auth = new JwtAuthenticationToken(jwt, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            // 検証失敗時はそのまま次へ（後続の authenticated() で弾かれる）
            filterChain.doFilter(request, response);
        } finally {
            ContextLocal.clear();
        }
    }

}
