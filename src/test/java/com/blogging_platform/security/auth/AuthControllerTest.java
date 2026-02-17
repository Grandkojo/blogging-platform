package com.blogging_platform.security.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.blogging_platform.ApiResponse;
import com.blogging_platform.security.jwt.TokenBlacklist;
import com.blogging_platform.service.UserService;

class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserService userService;

    @Mock
    private TokenBlacklist tokenBlacklist;

    @InjectMocks
    private AuthController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void login_returnsTokenPayload() {
        AuthDtos.LoginRequest req = new AuthDtos.LoginRequest("john@example.com", "password123");
        Authentication auth = new UsernamePasswordAuthenticationToken("john@example.com", "N/A");

        when(authenticationManager.authenticate(org.mockito.ArgumentMatchers.any(Authentication.class)))
                .thenReturn(auth);
        when(jwtService.generateToken(auth)).thenReturn("token");
        when(jwtService.getExpiresInSeconds()).thenReturn(3600L);

        ResponseEntity<ApiResponse<Object>> response = controller.login(req);

        assertEquals(HttpStatus.OK.value(), response.getBody().getStatus());
        verify(authenticationManager).authenticate(org.mockito.ArgumentMatchers.any(Authentication.class));
    }

    @Test
    void logout_revokesTokenWhenJwtAuthentication() {
        Jwt jwt = Jwt.withTokenValue("t")
                .header("alg", "HS256")
                .claim("roles", java.util.List.of("ROLE_READER"))
                .subject("john@example.com")
                .issuedAt(Instant.now().minusSeconds(10))
                .expiresAt(Instant.now().plusSeconds(60))
                .claim("jti", "jti-1")
                .build();

        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt);

        ResponseEntity<ApiResponse<Object>> response = controller.logout(auth);

        assertEquals(HttpStatus.OK.value(), response.getBody().getStatus());
        verify(tokenBlacklist).revoke("jti-1", jwt.getExpiresAt());
    }
}

