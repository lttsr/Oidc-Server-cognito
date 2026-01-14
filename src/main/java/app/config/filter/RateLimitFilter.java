package app.config.filter;

import java.io.IOException;
import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import app.config.RatingAceessConfig.RedisWrapper;
import app.context.exception.filter.TooManyRequestsException;
import app.context.http.RequestContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RedisWrapper redis;

    private static final RequestMatcher TARGET_MATCHER = new AntPathRequestMatcher("/api/auth/**");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (!TARGET_MATCHER.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

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
