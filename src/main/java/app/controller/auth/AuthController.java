package app.controller.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.usecase.auth.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

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
