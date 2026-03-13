package app.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 未認証時に 401 + JSON を返すエントリーポイント。
 * authenticated()メソッドによってアクセスが拒否された場合にAPIレスポンスを返します。
 */
public class UnAuthorizedEntryPoint implements AuthenticationEntryPoint {

    private static final String DEFAULT_MESSAGE = "Unauthorized";

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String message = authException != null && authException.getMessage() != null
                ? authException.getMessage()
                : DEFAULT_MESSAGE;
        String body = "{\"message\":\"" + escapeJson(message) + "\"}";
        response.getWriter().write(body);
    }

    private static String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
