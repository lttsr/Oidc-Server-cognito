package app.controller.auth;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
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
import app.usecase.userpool.UserPoolService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthEndpointController {

    private final AuthEndpointService authEndpointService;
    private final UserPoolService userPoolService;
    private final RedisWrapper redis;
    private final OrmRepository rep;

    private static final String SESSION_PREFIX = "auth:session:";

    /**
     * 認可エンドポイント - ログイン画面表示
     */
    @GetMapping("/init")
    public String initEndpoint(
            HttpServletRequest request,
            Model model) {
        try {
            // 保存済みリクエストを取得
            RequestCache requestCache = new HttpSessionRequestCache();
            SavedRequest savedRequest = requestCache.getRequest(request, null);

            // 初期化処理
            var initResult = authEndpointService.initEndpoint(savedRequest);
            List<UserPool> userPoolList = initResult.userPoolList();
            String companyLogoPath = initResult.companyLogoPath();

            String sessionId = UUID.randomUUID().toString();
            String sessionKey = SESSION_PREFIX + sessionId;

            // 認証開始を示すセッション情報を保存
            redis.template().opsForHash().put(sessionKey, "initialized", "true");
            redis.template().expire(sessionKey, Duration.ofMinutes(15));

            // モデルに追加
            model.addAttribute("sessionId", sessionId);
            model.addAttribute("user_pool_list", userPoolList);
            model.addAttribute("companyLogoPath", companyLogoPath);

            return "auth/login";
        } catch (Exception e) {
            model.addAttribute("error", "予期せぬエラーが発生しました。");
            return "redirect:/api/auth/init";
        }
    }

    /**
     * MFA認証画面表示
     */
    @GetMapping("/mfa")
    public String mfaPage(@RequestParam String sessionId, @RequestParam(required = false) String error, Model model) {
        String sessionKey = SESSION_PREFIX + sessionId;
        Map<Object, Object> authContext = redis.template().opsForHash().entries(sessionKey);

        if (authContext.isEmpty()) {
            return "redirect:/api/auth/init";
        }
        if (error != null) {
            model.addAttribute("error", "MFA verification failed");
        }
        model.addAttribute("username", authContext.get("username"));
        model.addAttribute("sessionId", sessionId);

        return "auth/mfa";
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
