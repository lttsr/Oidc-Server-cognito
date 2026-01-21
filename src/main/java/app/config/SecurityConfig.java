package app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import app.config.filter.ApiKeyVerificationFilter;
import app.config.filter.JwtVerificationFilter;
import app.config.filter.RateLimitFilter;
import app.config.filter.UserPoolContextFilter;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final RateLimitFilter rateLimitFilter;
    private final ApiKeyVerificationFilter apiKeyVerificationFilter;
    private final UserPoolContextFilter userPoolContextFilter;
    private final JwtVerificationFilter jwtVerificationFilter;

    /**
     * セキュリティ設定
     *
     * @param http
     * @return
     * @throws Exception
     */

    // TODO 管理職権限のみ許可設定
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(
                auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/userpool/**").permitAll()
                        .anyRequest().authenticated());
        http.csrf(AbstractHttpConfigurer::disable);
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // フィルター設定
        http.addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(apiKeyVerificationFilter, RateLimitFilter.class);
        http.addFilterAfter(userPoolContextFilter, ApiKeyVerificationFilter.class);
        http.addFilterAfter(jwtVerificationFilter, UserPoolContextFilter.class);

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login").permitAll()
                .anyRequest().authenticated());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
