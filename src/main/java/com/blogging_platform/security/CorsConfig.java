package com.blogging_platform.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Global CORS configuration for the blogging platform API.
 * Allows specific origins, methods, and headers so that browser and API clients
 * (e.g. React, JavaFX) can call the backend; requests from other origins are
 * not given Access-Control-Allow-Origin and are blocked by the browser.
 */
@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties props) {
        CorsConfiguration config = new CorsConfiguration();
        // Explicitly set allowed origins (empty = no cross-origin access; browser blocks requests from other origins)
        config.setAllowedOrigins(props.getAllowedOriginsList());
        if (!props.getAllowedMethodsList().isEmpty()) {
            config.setAllowedMethods(props.getAllowedMethodsList());
    } else {
            config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        }
        if (!props.getAllowedHeadersList().isEmpty()) {
            config.setAllowedHeaders(props.getAllowedHeadersList());
        } else {
            config.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type", "Accept"));
        }
        if (!props.getExposedHeadersList().isEmpty()) {
            config.setExposedHeaders(props.getExposedHeadersList());
        }
        if (props.getMaxAge() != null && props.getMaxAge() > 0) {
            config.setMaxAge(props.getMaxAge());
        }
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
