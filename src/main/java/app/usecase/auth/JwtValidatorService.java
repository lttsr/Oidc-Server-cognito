package app.usecase.auth;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import app.model.userpool.UserPool;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtValidatorService {

    private final Map<String, JwtDecoder> decoderCache = new ConcurrentHashMap<>();

    /**
     * トークンの正当性を検証する（署名・有効期限・Issuerのチェック）
     */
    public Jwt validate(String token, UserPool userPool) {
        // そのプール専用のDecoderを取得
        JwtDecoder decoder = decoderCache.computeIfAbsent(userPool.getUserPoolId(), id -> {
            String jwksUri = String.format("https://cognito-idp.%s.amazonaws.com/%s/.well-known/jwks.json",
                    userPool.getRegion(), id);
            return NimbusJwtDecoder.withJwkSetUri(jwksUri).build();
        });

        // 検証実行
        Jwt jwt = decoder.decode(token);

        // Issuerのチェック
        String expectedIssuer = String.format("https://cognito-idp.%s.amazonaws.com/%s",
                userPool.getRegion(), userPool.getUserPoolId());
        if (!expectedIssuer.equals(jwt.getIssuer().toString())) {
            throw new BadCredentialsException("Invalid token issuer");
        }

        return jwt;
    }
}
