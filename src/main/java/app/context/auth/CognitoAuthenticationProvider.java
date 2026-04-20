package app.context.auth;

import java.util.List;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.nimbusds.jwt.JWTParser;

import app.config.RedisConfig.RedisWrapper;
import app.context.cognito.ContextLocal;
import app.context.exception.InvalidCredentialsException;
import app.context.http.HttpStatusCode;
import app.context.orm.OrmRepository;
import app.model.userpool.UserPool;
import app.usecase.auth.AuthEndpointService;
import app.usecase.auth.JwtValidatorService;
import app.usecase.userpool.UserPoolService;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;

/**
 * Cognito認証を行うためのAuthenticationProvider
 * MFAチャレンジがある場合はMFAフローを継続します。
 * MFAチャレンジがない場合は、ログイン成功を返します。
 * 認証に必要なパラメータは以下の通りです。
 * ・username ユーザー名
 * ・password パスワード
 * ・userPoolAlias ユーザープールエイリアス
 * ・sessionId セッションID
 */

@Component
@RequiredArgsConstructor
public class CognitoAuthenticationProvider implements AuthenticationProvider {
    private static final String SESSION_PREFIX = "auth:session:";

    private final AuthEndpointService authEndpointService;
    private final UserPoolService userPoolService;
    private final JwtValidatorService jwtValidatorService;
    private final OrmRepository rep;
    private final RedisWrapper redis;

    /**
     * 認証処理を実施します。
     *
     * @param authentication 認証情報
     * @return 認証結果
     * @throws AuthenticationException
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            // ユーザ名
            String username = authentication.getName();
            // パスワード
            String password = authentication.getCredentials().toString();
            // 詳細情報
            CognitoAuthenticationDetails details = (CognitoAuthenticationDetails) authentication.getDetails();
            // ユーザープール情報
            String userPoolAlias = details.userPoolAlias();
            // セッションID
            String sessionId = details.sessionId();
            // セッションキー
            String sessionKey = SESSION_PREFIX + sessionId;

            if (redis.template().opsForHash().entries(sessionKey).isEmpty()) {
                throw new BadCredentialsException("Session expired");
            }

            // 選択されたユーザープール情報を取得
            UserPool userPool = rep.findBy(UserPool.class, "userPoolAlias", userPoolAlias).get(0);

            // ユーザープール情報をセット
            ContextLocal.setConfig(userPool);
            // セッション情報にユーザープールIDを保存
            redis.template().opsForHash().put(sessionKey, "user_pool_id", userPool.getUserPoolId());

            // AWS Cognito SDKを使用して認証を行います。
            InitiateAuthResponse response = authEndpointService.authUser(username, password);

            // MFAチャレンジがある場合はMFAフローを継続
            if (response.challengeName() != null) {
                redis.template().opsForHash().put(sessionKey, "mfa_session", response.session());
                redis.template().opsForHash().put(sessionKey, "username", username);
                redis.template().opsForHash().put(sessionKey, "challenge_name", response.challengeName().toString());
                return new MfaPendingAuthenticationToken(username, sessionId);
            }

            // CognitoのIDトークンを取得
            String idToken = response.authenticationResult().idToken();

            // tokenのIssuerを取得
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

        } catch (NotAuthorizedException e) {
            throw new InvalidCredentialsException(HttpStatusCode.BAD_REQUEST, e);
        } catch (Exception e) {
            throw new InvalidCredentialsException(HttpStatusCode.INTERNAL_SERVER_ERROR, e);
        } finally {
            ContextLocal.clear();
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    /**
     * 認証情報DTO
     *
     * @param sessionId     セッションID
     * @param userPoolAlias ユーザープールエイリアス
     */
    public record CognitoAuthenticationDetails(
            String sessionId,
            String userPoolAlias) {
    }
}
