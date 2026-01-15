package app.config.filter;

import java.io.IOException;
import java.util.Optional;

import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import app.context.cognito.ContextLocal;
import app.context.exception.filter.UserPoolContextException;
import app.context.messages.MessageUtils;
import app.context.orm.OrmRepository;
import app.model.cliant.CliantUserPoolBind;
import app.model.cliant.CliantUserPoolBind.CliantUserPoolBindId;
import app.model.userpool.UserPool;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/*
 * クライアントとユーザープール情報をセットするフィルター
 * クライアントIDとユーザープールIDをヘッダーから取得し、ユーザープール情報をセットします。
 * ユーザープール情報はContextLocalにセットされ、リクエストスコープで管理されます。
 * リクエストが完了次第、セットされた情報はクリアされます。
 */
@Component
@RequiredArgsConstructor
public class UserPoolContextFilter extends OncePerRequestFilter {

    private final OrmRepository ormRepository;

    private static final RequestMatcher API_AUTH_MATCHER = new AntPathRequestMatcher("/api/auth/**");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (!API_AUTH_MATCHER.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String cliantIdStr = request.getHeader("X-Cliant-Id");
            String userPoolIdStr = request.getHeader("X-User-Pool-Id");

            if (cliantIdStr == null || userPoolIdStr == null) {
                throw new UserPoolContextException(
                        MessageUtils.getMessage("userpool.context.error.required"));
            }

            Long cliantId = Long.parseLong(cliantIdStr);
            Long userPoolId = Long.parseLong(userPoolIdStr);

            // 企業とユーザープールのバインド情報を取得
            Optional<CliantUserPoolBind> bindOpt = ormRepository.get(
                    CliantUserPoolBind.class, CliantUserPoolBindId.of(cliantId,
                            userPoolId));

            if (bindOpt.isEmpty()) {
                throw new UserPoolContextException(
                        MessageUtils.getMessage("userpool.context.error.not_found"));
            }

            Optional<UserPool> userPoolOpt = ormRepository.get(UserPool.class, userPoolId);

            if (userPoolOpt.isEmpty()) {
                throw new UserPoolContextException(
                        MessageUtils.getMessage("userpool.context.error.not_found"));
            }

            ContextLocal.setConfig(userPoolOpt.get());

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            throw new UserPoolContextException();
        } finally {
            ContextLocal.clear();
        }
    }
}
