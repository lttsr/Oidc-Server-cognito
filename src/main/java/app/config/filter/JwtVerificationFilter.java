package app.config.filter;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;

import app.context.cognito.ContextLocal;
import app.model.userpool.UserPool;
import app.usecase.auth.JwtValidatorService;
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

    private final JwtValidatorService jwtValidatorService;
    private final UserPoolService userPoolService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // ログインエンドポイントは除外
        if (new AntPathRequestMatcher("/api/auth/**").matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        // トークンがないは次フィルターへ
        String token = jwtValidatorService.extractToken(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            JWT jwtClaims = JWTParser.parse(token);
            String issuer = jwtClaims.getJWTClaimsSet().getIssuer();

            // issuerからUserPoolを特定
            // 例: https://cognito-idp.ap-northeast-1.amazonaws.com/ap-northeast-1_xxxxxx
            UserPool userPool = userPoolService.findByIssuer(issuer);

            // JWT署名検証
            Jwt verifiedJwt = jwtValidatorService.validate(token, userPool);

            // ContextLocalへユーザプール情報をセット
            ContextLocal.setConfig(userPool);
            // SecurityContextへJwtAuthenticationTokenをセット
            Authentication auth = new JwtAuthenticationToken(verifiedJwt, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            filterChain.doFilter(request, response);
        } finally {
            ContextLocal.clear();
        }
    }

}
