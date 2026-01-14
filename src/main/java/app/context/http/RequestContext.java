package app.context.http;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * HTTP通信時のリクエスト情報を取得するクラス
 */
public class RequestContext {

    /**
     * クライアントのIPアドレスを取得します。
     *
     * @return IPアドレス
     */
    public static String getClientIpAddress() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return "-";
        }

        HttpServletRequest request = attributes.getRequest();

        // X-Forwarded-For ヘッダーチェック
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // 複数のプロキシを経由している場合、最初のIPを取得
            return ip.split(",")[0].trim();
        }
        // X-Real-IP ヘッダーチェック
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        // 直接接続の場合
        return request.getRemoteAddr();
    }

    /**
     * UserAgentを取得します。
     *
     * @return UserAgent
     */
    public static String getUserAgent() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return "-";
        }

        HttpServletRequest request = attributes.getRequest();
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent : "-";
    }

    /**
     * リクエストURIを取得します。
     *
     * @return リクエストURI
     */
    public static String getRequestUri() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return "-";
        }

        HttpServletRequest request = attributes.getRequest();
        return request.getRequestURI();
    }

    /**
     * HTTPメソッドを取得します。
     *
     * @return HTTPメソッド
     */
    public static String getHttpMethod() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return "-";
        }

        HttpServletRequest request = attributes.getRequest();
        return request.getMethod();
    }

    /**
     * HTTPリクエストヘッダーから指定されたヘッダー名の値を取得します。
     *
     * @param headerName ヘッダー名
     * @return ヘッダーの値。存在しない場合はnull
     */
    public static String extractHeader(String headerName) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return null;
        }

        HttpServletRequest request = attributes.getRequest();
        return request.getHeader(headerName);
    }
}
