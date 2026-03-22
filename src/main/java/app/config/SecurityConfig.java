package app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import app.config.filter.JwtVerificationFilter;
import app.config.filter.MfaCodeAuthenticationFilter;
import app.config.filter.RateLimitFilter;
import app.context.auth.CognitoAuthenticationProvider;
import app.context.auth.CognitoAuthenticationSuccessHandler;
import app.context.auth.CognitoMfaAuthenticationProvider;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final RateLimitFilter rateLimitFilter;
    private final JwtVerificationFilter jwtVerificationFilter;
    private final CognitoAuthenticationProvider cognitoAuthenticationProvider;
    private final CognitoAuthenticationSuccessHandler cognitoAuthenticationSuccessHandler;
    private final CognitoMfaAuthenticationProvider cognitoMfaAuthenticationProvider;
    private final MfaCodeAuthenticationFilter mfaCodeAuthenticationFilter;

    /**
     * セキュリティ設定
     *
     * @param http
     * @return
     * @throws Exception
     */

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = OAuth2AuthorizationServerConfigurer
                .authorizationServer().oidc(Customizer.withDefaults());

        http
                // Authorization Server endpoints のみに設定を適用
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .with(authorizationServerConfigurer, Customizer.withDefaults())
                // 未認証時 /api/auth/init へリダイレクト
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/api/auth/init")))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated());

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated())
                .authenticationProvider(cognitoAuthenticationProvider)
                .authenticationProvider(cognitoMfaAuthenticationProvider)
                .formLogin(form -> form
                        .loginPage("/api/auth/init")
                        .loginProcessingUrl("/api/auth/login")
                        .authenticationDetailsSource(
                                request -> new CognitoAuthenticationProvider.CognitoAuthenticationDetails(
                                        request.getParameter("sessionId"),
                                        request.getParameter("userPoolAlias")))
                        .successHandler(cognitoAuthenticationSuccessHandler)
                        .failureUrl("/api/auth/init?error")
                        .permitAll())
                .addFilterAfter(mfaCodeAuthenticationFilter, RateLimitFilter.class)
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtVerificationFilter, RateLimitFilter.class);

        return http.build();
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return context -> {
            if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {

                Authentication principal = context.getPrincipal();

                context.getClaims().claims(claims -> {
                    claims.put("sub", principal.getName());
                    if (principal instanceof UsernamePasswordAuthenticationToken auth) {
                        claims.put("user_pool_id", auth.getDetails().toString());
                    }
                });
            }
        };
    }

    @Bean
    public JwtDecoder selfJwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    /** 複数の発行者を有効化 */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .multipleIssuersAllowed(true)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
