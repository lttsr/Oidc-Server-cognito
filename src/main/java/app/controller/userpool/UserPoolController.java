package app.controller.userpool;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.usecase.userpool.UserPoolService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/userpool")
public class UserPoolController {

    private final UserPoolService userPoolService;

    // DescribeUserPool APIを使用して最新のユーザプール情報に更新します。
    @PostMapping("/attributes")
    public ResponseEntity<?> getUserPool() {
        var response = userPoolService.getUserPoolAttributes();
        return ResponseEntity.ok(response);
    }
}
