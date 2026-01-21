package app.config.filter;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt; // 修正
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken; // 追加
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import app.context.cognito.ContextLocal;
import app.context.messages.MessageUtils;
import app.model.userpool.UserPool;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
 * JWT検証フィルター
 * 指定されたURLに対して、JWT検証を行います。
 * JWTはヘッダーから取得されます。
 */
@Component
public class JwtVerificationFilter extends OncePerRequestFilter {

    // プールIDごとのDecoderをキャッシュする（毎回作ると重いため）
    private final Map<String, JwtDecoder> decoderCache = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        // ContextLocalからプール情報を取得（前のフィルターがセットしたもの）
        UserPool userPool = ContextLocal.getConfig();

        // そのプール専用のDecoderを取得または作成
        try {
            JwtDecoder decoder = decoderCache.computeIfAbsent(userPool.getUserPoolId(), id -> {
                String region = userPool.getRegion();
                String jwksUri = String.format("https://cognito-idp.%s.amazonaws.com/%s/.well-known/jwks.json", region,
                        id);
                return NimbusJwtDecoder.withJwkSetUri(jwksUri).build();
            });

            // 検証実行
            String token = extractToken(request); // Authorizationヘッダーから取得
            Jwt jwt = decoder.decode(token); // ここで鍵の検証が走る！

            // OKならSpringに「認証済み」と教える
            Authentication auth = new JwtAuthenticationToken(jwt, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            throw new AuthenticationServiceException(MessageUtils.getMessage("jwt.verification.error.failed"), e);
        }
    }

    /**
     * リクエストヘッダーから Authorization: Bearer <Token> を抽出するメソッド
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " の後ろから取得
        }
        return null;
    }
}
