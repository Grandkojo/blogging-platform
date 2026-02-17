package com.blogging_platform.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security baseline configuration.
 *
 * <p>This is intentionally minimal scaffolding for BEM-07. Subsequent feature
 * branches will add JWT authentication, OAuth2 login, RBAC, and stricter access
 * rules.</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Password encoder for hashing user passwords using BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Baseline security filter chain. Keeps existing public endpoints available
     * while security features are being introduced.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // JWT-based APIs will be stateless; CSRF will be configured properly later.
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                // Existing user auth endpoints (temporary public access)
                .requestMatchers(HttpMethod.POST, "/api/v1/users/register", "/api/v1/users/login").permitAll()
                // OpenAPI / Swagger UI
                .requestMatchers(
                    "/api/v1/swagger-ui.html",
                    "/api/v1/swagger-ui/**",
                    "/api/v1/v3/api-docs/**"
                ).permitAll()
                // Actuator (keep health/info open for now)
                .requestMatchers("/api/v1/actuator/health/**", "/api/v1/actuator/info").permitAll()
                // GraphQL UI in dev (GraphiQL is served under /graphiql by spring-graphql)
                .requestMatchers("/api/v1/graphiql", "/api/v1/graphiql/**").permitAll()
                // Everything else is temporarily permitted until JWT/OAuth2 is wired
                .anyRequest().permitAll()
            )
            // Keep defaults off (we'll add JWT and OAuth2 later)
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}

