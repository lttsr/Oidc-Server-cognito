package app.context.auth;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import app.context.exception.InvalidCredentialsException;
import app.context.http.HttpStatusCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CognitoAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {

        String sessionId = request.getParameter("sessionId");
        String clientId = request.getParameter("clientId");
        String errorCode = resolveErrorCode(exception);

        String redirectUrl = UriComponentsBuilder.fromPath("/api/auth/init")
                .queryParam("errorCode", errorCode)
                .queryParam("sessionId", sessionId)
                .queryParam("clientId", clientId)
                .build()
                .toUriString();
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private String resolveErrorCode(AuthenticationException exception) {
        if (exception instanceof InvalidCredentialsException invalidCredentialsException) {
            return invalidCredentialsException.getErrorCode();
        }
        return HttpStatusCode.INTERNAL_SERVER_ERROR;
    }
}
