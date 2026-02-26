package com.blogging_platform.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import com.blogging_platform.security.auth.GoogleOAuth2UserService;

/**
 * Spring Security baseline configuration.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationConverter jwtAuthenticationConverter;
    private final GoogleOAuth2UserService googleOAuth2UserService;

    public SecurityConfig(
            JwtAuthenticationConverter jwtAuthenticationConverter,
            GoogleOAuth2UserService googleOAuth2UserService) {
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
        this.googleOAuth2UserService = googleOAuth2UserService;
    }

    /**
     * Baseline security filter chain. Keeps existing public endpoints available
     * while security features are being introduced.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF: required only for /demo/** (form-style demo). JWT API is stateless and
                // does not use CSRF.
                .csrf(csrf -> csrf.requireCsrfProtectionMatcher(csrfDemoRequestMatcher()))
                // Global CORS uses CorsConfigurationSource bean from CorsConfig (allowed
                // origins/methods/headers from security.cors.*)
                .cors(Customizer.withDefaults())
                // STATELESS for JWT API, but OAuth2 login requires sessions for the OAuth2 flow
                // Sessions are created on demand for OAuth2 and /demo/** (CSRF token storage)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        /*
                         * IMPORTANT: the application uses server.servlet.context-path=/api/v1.
                         * Spring Security request matchers see the path *without* the context path,
                         * so external "/api/v1/auth/login" is matched here as "/auth/login".
                         */
                        // CSRF demo (session-based; no JWT required)
                        .requestMatchers("/demo/**").permitAll()
                        // Auth endpoints (JWT login/register)
                        .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/logout").authenticated()
                        // OpenAPI / Swagger UI
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**")
                        .permitAll()
                        // OAuth2 login – allow unauthenticated access to start and complete the OAuth2
                        // flow
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        // Actuator (keep health/info open for now)
                        .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                        // GraphQL UI in dev (GraphiQL is served under /graphiql by spring-graphql)
                        .requestMatchers("/graphiql", "/graphiql/**").permitAll()
                        // Performance testing endpoints
                        .requestMatchers("/performance/**").permitAll()
                        // GraphQL endpoint and all other APIs require authentication
                        .requestMatchers("/graphql").authenticated()
                        .anyRequest().authenticated())
                // OAuth2 login (Google) - enables /oauth2/authorization/google endpoint
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.oidcUserService(googleOAuth2UserService))
                        .defaultSuccessUrl("/swagger-ui.html", true)
                        .failureUrl("/swagger-ui.html?error=oauth2_failed"))
                // JWT validation happens via OAuth2 Resource Server support (wired in
                // JwtConfig)
                .oauth2ResourceServer(
                        oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));

        return http.build();
    }

    /**
     * CSRF is required only for state-changing requests to /demo/** (CSRF demo
     * endpoints).
     * All other paths (JWT API) are not subject to CSRF checks.
     */
    private static RequestMatcher csrfDemoRequestMatcher() {
        return request -> {
            // requestURI includes the context path (e.g. "/api/v1/demo/csrf-submit"),
            // but we want to match on the servlet path only ("/demo/...").
            String contextPath = request.getContextPath() != null ? request.getContextPath() : "";
            String path = request.getRequestURI().substring(contextPath.length());
            if (!path.startsWith("/demo/")) {
                return false;
            }
            String method = request.getMethod();
            return !HttpMethod.GET.matches(method)
                    && !HttpMethod.HEAD.matches(method)
                    && !HttpMethod.OPTIONS.matches(method)
                    && !"TRACE".equalsIgnoreCase(method);
        };
    }
}
