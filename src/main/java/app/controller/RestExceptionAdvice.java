package app.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import app.context.exception.filter.TooManyRequestsException;
import app.context.exception.filter.UserPoolContextException;
import app.context.messages.MessageUtils;

/**
 * アプリケーション例外ハンドラクラス
 */
@RestControllerAdvice
public class RestExceptionAdvice {

    /**
     * バリデーションエラー
     * Jakarta Bean Validationによる審査例外を処理します。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException e) {
        Map<String, String> messages = new HashMap<>();

        // 全てのフィールドエラーを処理
        e.getBindingResult().getFieldErrors()
                .forEach(err -> {
                    String msg = MessageUtils.getValidationMessage(
                            err.getField(),
                            err.getCode(),
                            err.getArguments(),
                            err.getDefaultMessage());
                    messages.put(err.getField(), msg);
                });

        ErrorResponse res = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                messages);

        return ResponseEntity.badRequest().body(res);
    }

    /**
     * Spring Security Filter TooManyRequestsException
     * レート制限を超えた場合の例外を処理します。
     */
    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ErrorResponse> handleTooManyRequestsException(
            TooManyRequestsException e) {
        ErrorResponse res = ErrorResponse.of(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "Too Many Requests",
                Map.of("message", e.getMessage()));
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(res);
    }

    /**
     * Spring Security Filter UserPoolContextException
     */
    @ExceptionHandler(UserPoolContextException.class)
    public ResponseEntity<ErrorResponse> handleUserPoolContextException(
            UserPoolContextException e) {
        ErrorResponse res = ErrorResponse.of(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                Map.of("message", e.getMessage()));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
    }

    /**
     * Spring Security Filter AuthenticationServiceException
     */
    @ExceptionHandler(AuthenticationServiceException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationServiceException(
            AuthenticationServiceException e) {
        ErrorResponse res = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                Map.of("message", e.getMessage()));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
    }
}
