package app.context.auth;

import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.nimbusds.jwt.JWTParser;

import app.config.RedisConfig.RedisWrapper;
import app.context.cognito.ContextLocal;
import app.context.orm.OrmRepository;
import app.model.userpool.UserPool;
import app.usecase.auth.AuthEndpointService;
import app.usecase.auth.JwtValidatorService;
import app.usecase.userpool.UserPoolService;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChallengeNameType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.RespondToAuthChallengeResponse;

@Component
@RequiredArgsConstructor
public class CognitoMfaAuthenticationProvider implements AuthenticationProvider {
    private static final String SESSION_PREFIX = "auth:session:";

    private final AuthEndpointService authEndpointService;
    private final UserPoolService userPoolService;
    private final OrmRepository rep;
    private final RedisWrapper redis;
    private final JwtValidatorService jwtValidatorService;

    @Override
    public Authentication authenticate(Authentication authentication) {
        if (!(authentication instanceof MfaCodeAuthenticationToken token)) {
            return null;
        }

        String sessionKey = SESSION_PREFIX + token.getSessionId();

        Map<Object, Object> sessionInfo = redis.template().opsForHash().entries(sessionKey);
        if (sessionInfo.isEmpty()) {
            throw new BadCredentialsException("Session expired");
        }

        try {
            String mfaSession = (String) sessionInfo.get("mfa_session");
            String username = (String) sessionInfo.get("username");
            String challengeNameRaw = (String) sessionInfo.get("challenge_name");
            String userPoolId = (String) sessionInfo.get("user_pool_id");

            if (mfaSession == null || username == null || challengeNameRaw == null || userPoolId == null) {
                throw new BadCredentialsException("MFA context is invalid");
            }

            // 選択されたユーザープール情報を取得
            UserPool userPool = rep.findBy(UserPool.class, "userPoolId", userPoolId).get(0);
            if (userPool == null) {
                throw new BadCredentialsException("UserPool not found");
            }

            // ユーザープール情報をセット
            ContextLocal.setConfig(userPool);

            RespondToAuthChallengeResponse response = authEndpointService.verifyMfaUser(
                    mfaSession,
                    String.valueOf(token.getCredentials()),
                    username,
                    ChallengeNameType.valueOf(challengeNameRaw));

            // CognitoのIDトークンを取得
            String idToken = response.authenticationResult().idToken();

            // Issuerを取得
            String issuer = JWTParser.parse(idToken).getJWTClaimsSet().getIssuer();

            // ユーザープールを取得
            UserPool pool = userPoolService.findByIssuer(issuer);

            // IDトークンを検証
            Jwt verifiedJwt = jwtValidatorService.validate(idToken, pool);

            // Spring Security認証トークンを作成
            UsernamePasswordAuthenticationToken authenticated = new UsernamePasswordAuthenticationToken(
                    verifiedJwt.getSubject(),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_USER")));

            authenticated.setDetails(pool.getUserPoolId());
            redis.template().delete(sessionKey);
            return authenticated;

        } catch (Exception e) {
            throw new BadCredentialsException("MFA authentication failed", e);

        } finally {
            ContextLocal.clear();
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return MfaCodeAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
