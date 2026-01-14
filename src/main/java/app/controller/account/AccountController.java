package app.controller.account;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.usecase.account.AccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final AccountService accountService;

    // ForgotPassword APIを使用して認証コードを送信します。
    @PostMapping("/password/reset")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid ForgotPasswordParams params) {
        var response = accountService.resetPassword(params.userName());
        return ResponseEntity.ok(response);
    }

    // ConfirmForgotPassword APIを使用して認証コードを確認します。
    @PostMapping("/password/confirm")
    public ResponseEntity<?> confirmResetPassword(@RequestBody @Valid ResetPasswordParams params) {
        accountService.confirmResetPassword(params.userName(), params.confirmationCode(), params.password());
        return ResponseEntity.ok().build();
    }

    // ChangePassword APIを使用してパスワードを変更します。
    @PostMapping("/password/change")
    public ResponseEntity<?> changePassword(@RequestBody @Valid ChangePasswordParams params) {
        accountService.changePassword(params.accessToken(), params.previousPassword(), params.proposedPassword());
        return ResponseEntity.ok().build();
    }

    // GetUser APIを使用してユーザー情報を取得します。
    @PostMapping("/user")
    public ResponseEntity<?> getUserInfo(@RequestBody @Valid GetUserParams params) {
        var response = accountService.getUserInfo(params.accessToken());
        return ResponseEntity.ok(response);
    }

    // UpdateUserAttributes APIを使用してユーザー属性を更新します。
    @PostMapping("/user/update")
    public ResponseEntity<?> updateUser(@RequestBody @Valid UpdateUserParams params) {
        accountService.updateUser(params.accessToken(), params.userAttributes());
        return ResponseEntity.ok().build();
    }

    // パスワードリセット要求パラメータ
    @Builder
    public record ForgotPasswordParams(
            /* ユーザー名 */
            @NotBlank @Size(min = 8, max = 64) String userName) {
    }

    // パスワードリセット確認パラメータ
    @Builder
    public record ResetPasswordParams(
            /* ユーザー名 */
            @NotBlank @Size(min = 8, max = 64) String userName,
            /* 認証コード */
            @NotBlank @Pattern(regexp = "^[0-9]{6}$") String confirmationCode,
            /* 新しいパスワード */
            @NotBlank @Size(min = 8, max = 64) String password) {
    }

    // パスワード変更パラメータ
    @Builder
    public record ChangePasswordParams(
            /* アクセストークン */
            @NotBlank String accessToken,
            /* 旧パスワード */
            @NotBlank @Size(min = 8, max = 64) String previousPassword,
            /* 新パスワード */
            @NotBlank @Size(min = 8, max = 64) String proposedPassword) {
    }

    // ユーザー情報取得パラメータ
    @Builder
    public record GetUserParams(
            /* アクセストークン */
            @NotBlank String accessToken) {
    }

    // ユーザー属性更新パラメータ
    @Builder
    public record UpdateUserParams(
            /* アクセストークン */
            @NotBlank String accessToken,
            /* ユーザー属性 */
            @NotEmpty Map<String, String> userAttributes) {
    }
}
