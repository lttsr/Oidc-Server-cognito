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
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final AccountService accountService;

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
