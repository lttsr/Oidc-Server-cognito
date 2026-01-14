package app.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import app.context.exception.InvalidAttributeException;
import app.context.messages.MessageUtils;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AliasExistsException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CodeDeliveryFailureException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CodeMismatchException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ExpiredCodeException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ForbiddenException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InternalErrorException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InvalidParameterException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InvalidPasswordException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InvalidUserPoolConfigurationException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.PasswordHistoryPolicyViolationException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.PasswordResetRequiredException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotConfirmedException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotFoundException;

/**
 * Cognito例外ハンドラクラス
 */
@RestControllerAdvice
public class CognitoExceptionAdvice {

    /**
     * ForbiddenExceptionが発生した場合の例外を処理します。
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(
            ForbiddenException e) {
        String message = MessageUtils.getMessage("cognito.error.forbidden");
        ErrorResponse res = ErrorResponse.of(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                Map.of("message", message));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
    }

    /**
     * InternalErrorExceptionが発生した場合の例外を処理します。
     */
    @ExceptionHandler(InternalErrorException.class)
    public ResponseEntity<ErrorResponse> handleInternalErrorException(
            InternalErrorException e) {
        String message = MessageUtils.getMessage("cognito.error.internal_error");
        ErrorResponse res = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                Map.of("message", message));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
    }

    /**
     * UserNotConfirmedExceptionが発生した場合の例外を処理します。
     */
    @ExceptionHandler(UserNotConfirmedException.class)
    public ResponseEntity<ErrorResponse> handleUserNotConfirmedException(
            UserNotConfirmedException e) {
        String message = MessageUtils.getMessage("cognito.error.user_not_confirmed");
        ErrorResponse res = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Not User Confirmed",
                Map.of("message", message));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    /**
     * UserNotFoundExceptionが発生した場合の例外を処理します。
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(
            UserNotFoundException e) {
        String message = MessageUtils.getMessage("cognito.error.user_not_found");
        ErrorResponse res = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                Map.of("message", message));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
    }

    /**
     * InvalidParameterExceptionが発生した場合の例外を処理します。
     */
    @ExceptionHandler(InvalidParameterException.class)
    public ResponseEntity<ErrorResponse> handleInvalidParameterException(
            InvalidParameterException e) {
        String message = MessageUtils.getMessage("cognito.error.invalid_parameter");
        ErrorResponse res = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid Parameter",
                Map.of("message", message));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    /**
     * NotAuthorizedExceptionが発生した場合の例外を処理します。
     */
    @ExceptionHandler(NotAuthorizedException.class)
    public ResponseEntity<ErrorResponse> handleNotAuthorizedException(
            NotAuthorizedException e) {
        String message = MessageUtils.getMessage("cognito.error.not_authorized");
        ErrorResponse res = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                "Not Authorized",
                Map.of("message", message));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
    }

    /**
     * CodeMismatchExceptionが発生した場合の例外を処理します。
     */
    @ExceptionHandler(CodeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleCodeMismatchException(
            CodeMismatchException e) {
        String message = MessageUtils.getMessage("cognito.error.code_mismatch");
        ErrorResponse res = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Code Mismatch",
                Map.of("message", message));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    /**
     * ExpiredCodeExceptionが発生した場合の例外を処理します。
     */
    @ExceptionHandler(ExpiredCodeException.class)
    public ResponseEntity<ErrorResponse> handleExpiredCodeException(
            ExpiredCodeException e) {
        String message = MessageUtils.getMessage("cognito.error.expired_code");
        ErrorResponse res = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Expired Code",
                Map.of("message", message));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    /**
     * InvalidUserPoolConfigurationExceptionが発生した場合の例外を処理します。
     */
    @ExceptionHandler(InvalidUserPoolConfigurationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUserPoolConfigurationException(
            InvalidUserPoolConfigurationException e) {
        String message = MessageUtils.getMessage("cognito.error.invalid_user_pool");
        ErrorResponse res = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid User Pool Configuration",
                Map.of("message", message));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    /**
     * PasswordResetRequiredExceptionが発生した場合の例外を処理します。
     */
    @ExceptionHandler(PasswordResetRequiredException.class)
    public ResponseEntity<ErrorResponse> handlePasswordResetRequiredException(
            PasswordResetRequiredException e) {
        String message = MessageUtils.getMessage("cognito.error.password_reset_required");
        ErrorResponse res = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Password Reset Required",
                Map.of("message", message));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    /**
     * AliasExistsExceptionが発生した場合の例外を処理します。
     */
    @ExceptionHandler(AliasExistsException.class)
    public ResponseEntity<ErrorResponse> handleAliasExistsException(
            AliasExistsException e) {
        String message = MessageUtils.getMessage("cognito.error.alias_exists");
        ErrorResponse res = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Alias Exists",
                Map.of("message", message));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    /**
     * CodeDeliveryFailureExceptionが発生した場合の例外を処理します。
     */
    @ExceptionHandler(CodeDeliveryFailureException.class)
    public ResponseEntity<ErrorResponse> handleCodeDeliveryFailureException(
            CodeDeliveryFailureException e) {
        String message = MessageUtils.getMessage("cognito.error.code_delivery_failure");
        ErrorResponse res = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Code Delivery Failure",
                Map.of("message", message));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    /**
     * InvalidPasswordExceptionが発生した場合の例外を処理します。
     */
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPasswordException(
            InvalidPasswordException e) {
        String message = MessageUtils.getMessage("cognito.error.invalid_password");
        ErrorResponse res = ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "Invalid Password",
                Map.of("message", message));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    /**
     * PasswordHistoryPolicyViolationExceptionが発生した場合の例外を処理します。
     */
    @ExceptionHandler(PasswordHistoryPolicyViolationException.class)
    public ResponseEntity<ErrorResponse> handlePasswordHistoryPolicyViolationException(
            PasswordHistoryPolicyViolationException e) {
        String message = MessageUtils.getMessage("cognito.error.history_policy_violation");
        ErrorResponse res = ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "History Policy Violation",
                Map.of("message", message));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    /**
     * InvalidAttributeExceptionが発生した場合の例外を処理します。
     */
    @ExceptionHandler(InvalidAttributeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAttributeException(
            InvalidAttributeException e) {
        ErrorResponse res = ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "Invalid Attribute",
                Map.of("message", e.getMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    /**
     * CognitoIdentityProviderExceptionが発生した場合の例外を処理します。
     */
    @ExceptionHandler(CognitoIdentityProviderException.class)
    public ResponseEntity<ErrorResponse> handleCognitoIdentityProviderException(
            CognitoIdentityProviderException e) {
        String message = MessageUtils.getMessage("cognito.error.internal_error");
        ErrorResponse res = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                Map.of("message", message));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
    }
}
