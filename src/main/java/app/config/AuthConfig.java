package app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;

import app.context.orm.OrmRepository;
import app.model.oidc.CompanyOauthConfig;
import lombok.AllArgsConstructor;

@Configuration
public class AuthConfig {

    @Component
    @AllArgsConstructor
    public static class CustomRegisteredClientRepository implements RegisteredClientRepository {
        private final OrmRepository rep;

        @Override
        public RegisteredClient findByClientId(String clientId) {
            return CompanyOauthConfig.findByClientId(rep, clientId)
                    .map(this::toRegisteredClient)
                    .orElse(null);
        }

        @Override
        public RegisteredClient findById(String id) {
            return findByClientId(id);
        }

        @Override
        public void save(RegisteredClient registeredClient) {
        }

        /**
         * RegisteredClient 型へ変換
         */
        public RegisteredClient toRegisteredClient(CompanyOauthConfig config) {
            return RegisteredClient.withId(config.getClientId())
                    .clientId(config.getClientId())
                    .clientSecret(config.getClientSecret())
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    .redirectUri(config.getRedirectUris())
                    .scope(config.getScopes())
                    .build();
        }
    }

}
