package com.blogging_platform.security;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for global CORS (Cross-Origin Resource Sharing).
 * Used to allow specific origins, methods, and headers for browser and API clients
 * (e.g. React, JavaFX) while blocking unauthorized origins.
 */
@ConfigurationProperties(prefix = "security.cors")
public class CorsProperties {

    /**
     * Comma-separated allowed origins (e.g. http://localhost:3000, https://myapp.example.com).
     * If empty, cross-origin requests from browsers will not receive
     * Access-Control-Allow-Origin and will be blocked by the browser.
     */
    private String allowedOrigins = "";

    /**
     * Comma-separated HTTP methods. Defaults to GET, POST, PUT, PATCH, DELETE, OPTIONS.
     */
    private String allowedMethods = "GET,POST,PUT,PATCH,DELETE,OPTIONS";

    /**
     * Comma-separated allowed request headers. Defaults to Authorization, Content-Type, Accept.
     */
    private String allowedHeaders = "Authorization,Content-Type,Accept";

    /**
     * Comma-separated response headers to expose to the client. Empty by default.
     */
    private String exposedHeaders = "";

    /**
     * Max age (seconds) for preflight cache. Default 3600.
     */
    private Long maxAge = 3600L;

    public String getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(String allowedOrigins) {
        this.allowedOrigins = allowedOrigins != null ? allowedOrigins : "";
    }

    /** Returns allowed origins as a list (empty if not configured). */
    public List<String> getAllowedOriginsList() {
        return split(allowedOrigins);
    }

    public String getAllowedMethods() {
        return allowedMethods;
    }

    public void setAllowedMethods(String allowedMethods) {
        this.allowedMethods = allowedMethods != null ? allowedMethods : "";
    }

    public List<String> getAllowedMethodsList() {
        return split(allowedMethods);
    }

    public String getAllowedHeaders() {
        return allowedHeaders;
    }

    public void setAllowedHeaders(String allowedHeaders) {
        this.allowedHeaders = allowedHeaders != null ? allowedHeaders : "";
    }

    public List<String> getAllowedHeadersList() {
        return split(allowedHeaders);
    }

    public String getExposedHeaders() {
        return exposedHeaders;
    }

    public void setExposedHeaders(String exposedHeaders) {
        this.exposedHeaders = exposedHeaders != null ? exposedHeaders : "";
    }

    public List<String> getExposedHeadersList() {
        return split(exposedHeaders);
    }

    public Long getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Long maxAge) {
        this.maxAge = maxAge != null ? maxAge : 3600L;
    }

    private static List<String> split(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
