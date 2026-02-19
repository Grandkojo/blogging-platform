package com.blogging_platform.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Verifies CORS configuration: CorsProperties parsing and that CorsConfigurationSource
 * allows only configured origins (unauthorized origins are not in the list).
 */
class CorsConfigTest {

    @Test
    void corsProperties_splitsAllowedOrigins() {
        CorsProperties props = new CorsProperties();
        props.setAllowedOrigins("http://localhost:3000, http://localhost:5173");
        assertEquals(2, props.getAllowedOriginsList().size());
        assertTrue(props.getAllowedOriginsList().contains("http://localhost:3000"));
        assertTrue(props.getAllowedOriginsList().contains("http://localhost:5173"));
    }

    @Test
    void corsProperties_emptyOrigins_returnsEmptyList() {
        CorsProperties props = new CorsProperties();
        props.setAllowedOrigins("");
        assertTrue(props.getAllowedOriginsList().isEmpty());
    }

    @Test
    void corsConfigurationSource_usesOnlyConfiguredOrigins() {
        CorsProperties props = new CorsProperties();
        props.setAllowedOrigins("http://localhost:3000,http://localhost:5173");
        props.setAllowedMethods("GET,POST,OPTIONS");
        props.setAllowedHeaders("Authorization,Content-Type,Accept");

        CorsConfig config = new CorsConfig();
        UrlBasedCorsConfigurationSource source = (UrlBasedCorsConfigurationSource) config.corsConfigurationSource(props);

        CorsConfiguration cors = source.getCorsConfiguration(
            new org.springframework.mock.web.MockHttpServletRequest("GET", "/api/v1/posts"));
        assertNotNull(cors);
        assertEquals(2, cors.getAllowedOrigins().size());
        assertTrue(cors.getAllowedOrigins().contains("http://localhost:3000"));
        assertTrue(cors.getAllowedOrigins().contains("http://localhost:5173"));
        assertTrue(cors.getAllowedMethods().contains("GET"));
        assertTrue(cors.getAllowedHeaders().contains("Authorization"));
    }

    @Test
    void corsConfigurationSource_emptyOrigins_allowsNoCrossOrigin() {
        CorsProperties props = new CorsProperties();
        props.setAllowedOrigins("");

        CorsConfig config = new CorsConfig();
        UrlBasedCorsConfigurationSource source = (UrlBasedCorsConfigurationSource) config.corsConfigurationSource(props);

        CorsConfiguration cors = source.getCorsConfiguration(
            new org.springframework.mock.web.MockHttpServletRequest("GET", "/api/v1/posts"));
        assertNotNull(cors);
        // When no origins configured, we set an empty list so no origin gets Access-Control-Allow-Origin
        assertTrue(cors.getAllowedOrigins().isEmpty());
    }

    @Test
    void corsProperties_splitsMethodsAndHeaders() {
        CorsProperties props = new CorsProperties();
        props.setAllowedMethods("GET, POST, PUT, OPTIONS");
        props.setAllowedHeaders("Authorization, Content-Type");

        assertEquals(4, props.getAllowedMethodsList().size());
        assertTrue(props.getAllowedMethodsList().contains("GET"));
        assertTrue(props.getAllowedMethodsList().contains("OPTIONS"));
        assertEquals(2, props.getAllowedHeadersList().size());
        assertTrue(props.getAllowedHeadersList().contains("Authorization"));
        assertTrue(props.getAllowedHeadersList().contains("Content-Type"));
    }

    @Test
    void corsConfigurationSource_respectsMaxAge() {
        CorsProperties props = new CorsProperties();
        props.setAllowedOrigins("http://localhost:3000");
        props.setMaxAge(7200L);

        CorsConfig config = new CorsConfig();
        UrlBasedCorsConfigurationSource source = (UrlBasedCorsConfigurationSource) config.corsConfigurationSource(props);

        CorsConfiguration cors = source.getCorsConfiguration(
            new org.springframework.mock.web.MockHttpServletRequest("GET", "/api/v1/posts"));
        assertNotNull(cors);
        assertEquals(Long.valueOf(7200L), cors.getMaxAge());
    }

    @Test
    void corsConfigurationSource_exposedHeaders() {
        CorsProperties props = new CorsProperties();
        props.setAllowedOrigins("http://localhost:3000");
        props.setExposedHeaders("X-Total-Count, X-Request-Id");

        CorsConfig config = new CorsConfig();
        UrlBasedCorsConfigurationSource source = (UrlBasedCorsConfigurationSource) config.corsConfigurationSource(props);

        CorsConfiguration cors = source.getCorsConfiguration(
            new org.springframework.mock.web.MockHttpServletRequest("GET", "/api/v1/posts"));
        assertNotNull(cors);
        assertTrue(cors.getExposedHeaders().contains("X-Total-Count"));
        assertTrue(cors.getExposedHeaders().contains("X-Request-Id"));
    }
}
