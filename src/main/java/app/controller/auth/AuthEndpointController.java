package app.controller.auth;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import app.config.RedisConfig.RedisWrapper;
import app.context.cognito.ContextLocal;
import app.context.orm.OrmRepository;
import app.model.userpool.UserPool;
import app.usecase.auth.AuthEndpointService;
import app.usecase.company.CompanyLogoService;
import app.usecase.userpool.UserPoolService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChallengeNameType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.RespondToAuthChallengeResponse;

@Controller
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthEndpointController {

    private final AuthEndpointService authEndpointService;
    private final UserPoolService userPoolService;
    private final RedisWrapper redis;
    private final OrmRepository rep;
    private final CompanyLogoService companyLogoService;

    private static final String SESSION_PREFIX = "auth:session:";

    /**
     * 認可エンドポイント - ログイン画面表示
     */
    @GetMapping("/init")
    public String initEndpoint(@Valid AuthorizeParams params, Model model) {

        var userPoolList = authEndpointService.initEndpoint(params.companyId());

        var companyLogoPath = companyLogoService.findLogoPathByCompanyId(params.companyId())
                .orElse(null);

        String sessionId = UUID.randomUUID().toString();
        String sessionKey = SESSION_PREFIX + sessionId;

        Map<String, String> data = new HashMap<>();
        data.put("client_id", params.companyId().toString());
        if (params.redirectUri() != null) {
            data.put("redirect_uri", params.redirectUri());
        }
        if (params.state() != null) {
            data.put("state", params.state());
        }

        // セッション情報を保存
        redis.template().opsForHash().putAll(sessionKey, data);
        redis.template().expire(sessionKey, Duration.ofMinutes(15));

        // モデルに追加
        model.addAttribute("sessionId", sessionId);
        model.addAttribute("user_pool_list", userPoolList);
        model.addAttribute("companyLogoPath", companyLogoPath);

        return "auth/login";
    }

    /**
     * 認可エンドポイント - パラメータ
     */
    @Builder
    public record AuthorizeParams(
            @NotNull Long companyId,
            String redirectUri,
            String state) {
    }

    /**
     * ログイン処理
     */
    @PostMapping("/login")
    public String login(@Valid LoginParams params, Model model) {

        try {
            // セッションキーを生成
            String sessionKey = SESSION_PREFIX + params.sessionId();
            // Redisからセッション情報を取得
            Map<Object, Object> authContext = redis.template().opsForHash().entries(sessionKey);

            if (authContext.isEmpty()) {
                // 不正なUUID
                // TODO
                throw new Exception("Session expired");
            }
            String redirectUri = (String) authContext.get("redirect_uri");
            String state = (String) authContext.get("state");

            // AliasからUserPoolを取得
            List<UserPool> userPoolList = rep.findBy(UserPool.class, "userPoolAlias", params.userPoolAlias());
            if (userPoolList.isEmpty()) {
                throw new Exception("UserPool not found");
            }
            UserPool userPool = userPoolList.get(0);

            ContextLocal.setConfig(userPool);
            // UserPoolIDをセッションに保存
            redis.template().opsForHash().put(sessionKey, "user_pool_id", userPool.getUserPoolId());

            // Cognito認証（InitiateAuth USER_PASSWORD_AUTH）
            InitiateAuthResponse response = authEndpointService.authUser(params.username(),
                    params.password());

            // MFAが必要な場合
            if (response.challengeName() != null) {
                // MFA情報を追加
                redis.template().opsForHash().put(sessionKey, "mfa_session", response.session());
                redis.template().opsForHash().put(sessionKey, "username", params.username());
                redis.template().opsForHash().put(sessionKey, "challenge_name",
                        response.challengeName().toString());

                return "redirect:/api/auth/mfa?session_id=" + params.sessionId();
            }

            // 認証成功 - トークンをリダイレクトで返す
            AuthenticationResultType authResult = response.authenticationResult();

            // リダイレクトURLの構築
            String redirectUrl = UriComponentsBuilder.fromUriString(redirectUri) // 元のURL //
                                                                                 // (https://app.com/callback)
                    .fragment(String.format(
                            "id_token=%s&access_token=%s&refresh_token=%s&token_type=Bearer&expires_in=%s&state=%s",
                            authResult.idToken(),
                            authResult.accessToken(),
                            authResult.refreshToken(),
                            authResult.expiresIn(),
                            state))
                    .build()
                    .toUriString();

            // 使用済みのセッションを削除してリダイレクト
            redis.template().delete(sessionKey);

            return "redirect:" + redirectUrl;

        } catch (Exception e) {
            model.addAttribute("error", "Login failed");
            return "error";
        } finally {
            ContextLocal.clear();
        }
    }

    /**
     * ログインパラメータ
     */
    @Builder
    public record LoginParams(
            /* セッションID */
            @NotBlank String sessionId,
            /* ユーザープールエイリアス */
            @NotBlank String userPoolAlias,
            /* ユーザー名 */
            @NotBlank @Size(min = 8, max = 64) String username,
            /* パスワード */
            @NotBlank @Size(min = 8, max = 64) String password) {
    }

    /**
     * MFA認証画面表示
     */
    @GetMapping("/mfa")
    public String mfaPage(@RequestParam String sessionId, Model model) {
        String sessionKey = SESSION_PREFIX + sessionId;
        Map<Object, Object> authContext = redis.template().opsForHash().entries(sessionKey);

        if (authContext.isEmpty()) {
            return "redirect:/api/auth/init";
        }
        model.addAttribute("username", authContext.get("username"));
        model.addAttribute("sessionId", sessionId);

        return "auth/mfa";
    }

    /**
     * MFA検証処理
     */
    @PostMapping("/verify-mfa")
    public String verifyMfa(@Valid MfaParams params, Model model) {

        try {
            // セッションキーを生成
            String sessionKey = SESSION_PREFIX + params.sessionId();
            // Redisからセッション情報を取得
            Map<Object, Object> authContext = redis.template().opsForHash().entries(sessionKey);

            if (authContext.isEmpty()) {
                // 不正なUUID
                // TODO
                throw new Exception("Session expired");
            }
            String mfaSession = (String) authContext.get("mfa_session"); // MFAセッションID
            String username = (String) authContext.get("username"); // ユーザー名
            ChallengeNameType challengeName = ChallengeNameType.valueOf((String) authContext.get("challenge_name")); // 認証フロー
            String redirectUri = (String) authContext.get("redirect_uri"); // リダイレクトURL
            String state = (String) authContext.get("state"); // 状態

            // RespondToAuthChallenge で MFA 認証を行います。
            RespondToAuthChallengeResponse response = authEndpointService.verifyMfaUser(
                    mfaSession, params.mfaCode(), username, challengeName);

            // 認証成功 - トークンを取得
            AuthenticationResultType authResult = response.authenticationResult();

            // リダイレクトURLの構築
            String redirectUrl = UriComponentsBuilder.fromUriString(redirectUri)
                    .fragment(String.format("id_token=%s&access_token=%s&state=%s",
                            authResult.idToken(), authResult.accessToken(), state))
                    .build().toUriString();

            // 使用済みのセッションを削除してリダイレクト
            redis.template().delete(sessionKey);

            return "redirect:" + redirectUrl;

        } catch (Exception e) {
            model.addAttribute("error", "MFA verification failed");
            return "error";
        } finally {
            ContextLocal.clear();
        }
    }

    @Builder
    public record MfaParams(
            @NotBlank String sessionId,
            @NotBlank @Pattern(regexp = "^[0-9]{6}$") String mfaCode) {
    }

    // 既に発行されているトークンをリフレッシュします。
    @PostMapping("/refresh")
    public String refresh(@RequestBody @Valid RefreshParams params) {
        var response = authEndpointService.refreshToken(params.refreshToken());
        // リダイレクトURLの構築
        String redirectUrl = UriComponentsBuilder.fromUriString(params.redirectUri())
                .fragment(String.format("id_token=%s&access_token=%s&refresh_token=%s&token_type=Bearer&expires_in=%s",
                        response.authenticationResult().idToken(),
                        response.authenticationResult().accessToken(),
                        response.authenticationResult().refreshToken(),
                        response.authenticationResult().expiresIn()))
                .build().toUriString();

        return "redirect:" + redirectUrl;
    }

    // リフレッシュトークンパラメータ
    @Builder
    public record RefreshParams(
            /* リフレッシュトークン */
            @NotBlank String refreshToken,
            /* リダイレクトURL */
            String redirectUri) {
    }

    // パスワードリセット画面表示
    @GetMapping("/forgot-password")
    public String forgotPasswordPage(@RequestParam Long companyId, Model model) {
        try {
            // 企業IDに紐づく全てのUserPoolを取得
            var userPoolList = userPoolService.findAllByCompanyId(companyId);
            model.addAttribute("user_pool_list", userPoolList);
            model.addAttribute("companyId", companyId);
            return "auth/forgot-password";

        } catch (Exception e) {
            model.addAttribute("error", "予期せぬエラーが発生しました。");
            return "redirect:/api/auth/forgot-password?companyId=" + companyId;
        }
    }

    // ForgotPassword APIを使用して認証コードを送信します。
    @PostMapping("/forgot-password")
    public String resetPassword(@Valid ForgotPasswordParams params, Model model) {
        try {
            // AliasからUserPoolを取得
            List<UserPool> userPoolList = rep.findBy(UserPool.class, "userPoolAlias", params.userPoolAlias());
            if (userPoolList.isEmpty()) {
                model.addAttribute("error", "UserPool not found");
                return "redirect:/api/auth/forgot-password?companyId=" + params.companyId();
            }
            UserPool userPool = userPoolList.get(0);

            // UserPoolをセット
            ContextLocal.setConfig(userPool);

            // ForgotPassword APIを使用して認証コードを送信
            var res = authEndpointService.resetPassword(params.userName());

            // 認証コード送信成功
            if (res.codeDeliveryDetails() != null) {
                String authInfo = res.codeDeliveryDetails().destination();
                model.addAttribute("authInfo", authInfo);
                model.addAttribute("companyId", params.companyId());
                model.addAttribute("userPoolAlias", params.userPoolAlias());
                model.addAttribute("userName", params.userName());
                return "auth/forgot-password-confirm";
            } else {
                model.addAttribute("error", "認証コードの送信に失敗しました。");
                return "redirect:/api/auth/forgot-password?companyId=" + params.companyId();
            }
        } catch (Exception e) {
            model.addAttribute("error", "予期せぬエラーが発生しました。");
            return "redirect:/api/auth/forgot-password?companyId=" + params.companyId();
        } finally {
            ContextLocal.clear();
        }
    }

    // パスワードリセット要求パラメータ
    @Builder
    public record ForgotPasswordParams(
            /* ユーザー名 */
            @NotBlank @Size(min = 8, max = 64) String userName,
            /* ユーザープールエイリアス */
            @NotBlank String userPoolAlias,
            /* 企業ID */
            @NotNull Long companyId) {
    }

    // ConfirmForgotPassword APIを使用して認証コードを確認します。
    @PostMapping("/forgot-password/confirm")
    public String confirmResetPassword(@RequestBody @Valid ResetPasswordParams params, Model model) {
        try {
            authEndpointService.confirmResetPassword(params.userName(), params.confirmationCode(), params.password());
            model.addAttribute("companyId", params.companyId());
            return "auth/forgot-password-success";
        } catch (Exception e) {
            model.addAttribute("error", "予期せぬエラーが発生しました。");
            return "redirect:/api/auth/forgot-password/confirm?companyId=" + params.companyId();
        }
    }

    // パスワードリセット確認パラメータ
    @Builder
    public record ResetPasswordParams(
            /* 企業ID */
            @NotNull Long companyId,
            /* ユーザー名 */
            @NotBlank @Size(min = 8, max = 64) String userName,
            /* 認証コード */
            @NotBlank @Pattern(regexp = "^[0-9]{6}$") String confirmationCode,
            /* 新しいパスワード */
            @NotBlank @Size(min = 8, max = 64) String password) {
    }

}
