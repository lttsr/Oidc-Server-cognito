package app.controller.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.usecase.auth.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChallengeNameType;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    // AdminInitiateAuth APIを使用した利用者認証を行います。
    @PostMapping("/login")
    public ResponseEntity<?> auth(@RequestBody @Valid AuthParams params) {
        var response = authService.authUser(params.userName(), params.password());
        return ResponseEntity.ok(response);

    }

    // RespondToAuthChallenge APIを使用したMFA認証を行います。
    @PostMapping("/verify-mfa")
    public ResponseEntity<?> verifyMfa(@RequestBody @Valid MfaVerifyParams params) {
        var response = authService.verifyMfaUser(
                params.session(),
                params.mfaCode(),
                params.userName(),
                params.challengeName());
        return ResponseEntity.ok(response);
    }

    // 既に発行されているトークンをリフレッシュします。
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody @Valid RefreshParams params) {
        var response = authService.refreshToken(params.refreshToken());
        return ResponseEntity.ok(response);
    }

    // RevokeToken APIを使用してトークンを無効化します。
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody @Valid LogoutParams params) {
        authService.logout(params.token());
        return ResponseEntity.ok().build();
    }

    // GlobalSignOut APIを使用してトークンを無効化します。
    @PostMapping("/global-logout")
    public ResponseEntity<?> globalLogout(@RequestBody @Valid GlobalLogoutParams params) {
        authService.globalLogout(params.accessToken());
        return ResponseEntity.ok().build();
    }

    // 利用者認証パラメタ
    @Builder
    public record AuthParams(
            /* ユーザー名 */
            @NotBlank @Size(min = 8, max = 64) String userName,
            /* パスワード */
            @NotBlank @Size(min = 8, max = 64) String password) {
    }

    // MFA認証パラメタ
    @Builder
    public record MfaVerifyParams(
            /* セッションID */
            @NotBlank @Size(min = 20, max = 2048) String session,
            /* MFAコード */
            @NotBlank @Pattern(regexp = "^[0-9]{6}$") String mfaCode,
            /* ユーザー名 */
            @NotBlank @Size(min = 8, max = 64) String userName,
            /* 認証フロー */
            @NotNull ChallengeNameType challengeName) {
    }

    // リフレッシュトークンパラメータ
    @Builder
    public record RefreshParams(
            /* リフレッシュトークン */
            @NotBlank String refreshToken) {
    }

    // ログアウトパラメータ
    @Builder
    public record LogoutParams(
            /* トークン */
            @NotBlank String token) {
    }

    // グローバルログアウトパラメータ
    @Builder
    public record GlobalLogoutParams(
            /* アクセストークン */
            @NotBlank String accessToken) {
    }
}
