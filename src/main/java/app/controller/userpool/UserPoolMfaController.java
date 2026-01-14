package app.controller.userpool;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.controller.userpool.type.MfaConfigType;
import app.usecase.userpool.UserPoolMfaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/userpool/mfa")
public class UserPoolMfaController {
    private final UserPoolMfaService userPoolMfaService;

    @PostMapping("/config")
    public ResponseEntity<?> setMfaConfig(@RequestBody @Valid @NotNull MfaConfigParam configType) {
        var response = userPoolMfaService.setMfaConfig(configType.configType());
        return ResponseEntity.ok(response);
    }

    @Builder
    public record MfaConfigParam(
            /* MFA設定種別 */
            MfaConfigType configType
    // /* メールMFA設定 */
    // EmailMfaConfiguration emailMfaConfiguration,
    // /* SMSMFA設定 */
    // SmsMfaConfiguration smsMfaConfiguration
    ) {
    }

}
