package app.config.filter;

import java.io.IOException;
import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import app.config.RedisConfig.RedisWrapper;
import app.context.exception.filter.TooManyRequestsException;
import app.context.http.RequestContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/*
 * アクセスレート制限フィルター
 * アクセスレート制限はRedisを使用し、IPアドレス単位で管理されます。
 * アクセスレート制限は指定された期間内に指定された回数を超えた場合、TooManyRequestsExceptionを投げます。
 */
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RedisWrapper redis;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        return path.startsWith("/api/auth/")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String ipAddress = RequestContext.getClientIpAddress();

        if (!isAllowed(ipAddress)) {
            throw new TooManyRequestsException();
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAllowed(String key) {
        StringRedisTemplate tmp = redis.template();
        int rateLimit = redis.rateLimit();
        int rateTime = redis.rateTime();

        String redisKey = "access_count:" + key;

        String countStr = tmp.opsForValue().get(redisKey);
        int count = countStr != null ? Integer.parseInt(countStr) : 0;

        if (count >= rateLimit) {
            return false;
        }

        if (count == 0) {
            tmp.opsForValue().set(redisKey, "1", Duration.ofMillis(rateTime));
        } else {
            tmp.opsForValue().increment(redisKey);
        }
        return true;
    }
}
