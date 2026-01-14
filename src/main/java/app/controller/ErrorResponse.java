package app.controller;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;

/**
 * エラーレスポンスDTO
 */
@Builder
public record ErrorResponse(
        int status,
        String error,
        Map<String, String> messages,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime timestamp) {

    public static ErrorResponse of(int status, String err, Map<String, String> messages) {
        return ErrorResponse.builder()
                .status(status)
                .error(err)
                .messages(messages)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
