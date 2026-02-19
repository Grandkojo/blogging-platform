package com.blogging_platform.security.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.Duration;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;

import com.blogging_platform.security.jwt.JwtProperties;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

class JwtServiceTest {

    @Test
    void generateToken_includesExpectedClaims_andIsDecodable() {
        JwtProperties props = new JwtProperties();
        props.setSecret("test-secret-test-secret-test-secret-test-secret");
        props.setIssuer("https://blogging-platform");
        props.setTtl(Duration.ofMinutes(10));

        SecretKey key = new SecretKeySpec(props.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer(props.getIssuer())));

        JwtService jwtService = new JwtService(encoder, props);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "john@example.com",
                "N/A",
                List.of(new SimpleGrantedAuthority("ROLE_READER"))
        );

        String token = jwtService.generateToken(auth);
        Jwt jwt = decoder.decode(token);

        assertEquals("john@example.com", jwt.getSubject());
        assertEquals(props.getIssuer(), jwt.getIssuer().toString());
        assertNotNull(jwt.getIssuedAt());
        assertNotNull(jwt.getExpiresAt());

        List<String> roles = jwt.getClaimAsStringList("roles");
        assertTrue(roles.contains("ROLE_READER"));
        assertNotNull(jwt.getId());
    }

    @Test
    void decode_fails_whenTokenIsTampered() {
        JwtProperties props = new JwtProperties();
        props.setSecret("test-secret-test-secret-test-secret-test-secret");
        props.setIssuer("https://blogging-platform");
        props.setTtl(Duration.ofMinutes(10));

        SecretKey key = new SecretKeySpec(props.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();

        JwtService jwtService = new JwtService(encoder, props);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "john@example.com",
                "N/A",
                List.of(new SimpleGrantedAuthority("ROLE_READER"))
        );

        String token = jwtService.generateToken(auth);

        assertThrows(JwtException.class, () -> decoder.decode(token + "x"));
    }

    @Test
    void decode_fails_whenTokenIsExpired() {
        JwtProperties props = new JwtProperties();
        props.setSecret("test-secret-test-secret-test-secret-test-secret");
        props.setIssuer("https://blogging-platform");
        props.setTtl(Duration.ofMinutes(10));

        SecretKey key = new SecretKeySpec(props.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer(props.getIssuer())));

        // Create an already-expired token (issuedAt < expiresAt < now).
        Instant issuedAt = Instant.now().minusSeconds(7200);
        Instant expiresAt = Instant.now().minusSeconds(3600);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(props.getIssuer())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject("john@example.com")
                .id("jti-expired")
                .claim("roles", List.of("ROLE_READER"))
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256)
                .type("JWT")
                .build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        assertThrows(JwtValidationException.class, () -> decoder.decode(token));
    }

    @Test
    void generateToken_withMultipleRoles_includesAllInClaims() {
        JwtProperties props = new JwtProperties();
        props.setSecret("test-secret-test-secret-test-secret-test-secret");
        props.setIssuer("https://blogging-platform");
        props.setTtl(Duration.ofMinutes(10));

        SecretKey key = new SecretKeySpec(props.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(props.getIssuer())));

        JwtService jwtService = new JwtService(encoder, props);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "admin@example.com",
                "N/A",
                List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("ROLE_READER")
                )
        );

        String token = jwtService.generateToken(auth);
        Jwt jwt = decoder.decode(token);

        assertEquals("admin@example.com", jwt.getSubject());
        List<String> roles = jwt.getClaimAsStringList("roles");
        assertTrue(roles.contains("ROLE_ADMIN"));
        assertTrue(roles.contains("ROLE_READER"));
        assertEquals(2, roles.size());
    }
}

