package com.blogging_platform.security.auth;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.blogging_platform.ApiResponse;
import com.blogging_platform.model.User;
import com.blogging_platform.security.jwt.TokenBlacklist;
import com.blogging_platform.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST authentication endpoints for JWT-based access.
 *
 * <p>Note: external paths are prefixed with the application's context path
 * (server.servlet.context-path=/api/v1).</p>
 */
@RestController
@Tag(name = "Auth", description = "JWT authentication endpoints")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final TokenBlacklist tokenBlacklist;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserService userService,
            TokenBlacklist tokenBlacklist
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
        this.tokenBlacklist = tokenBlacklist;
    }

    @Operation(summary = "Login and receive a JWT")
    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse<Object>> login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        String token = jwtService.generateToken(auth);
        AuthDtos.AuthResponse payload = new AuthDtos.AuthResponse(token, "Bearer", jwtService.getExpiresInSeconds());

        String principal = auth.getPrincipal() instanceof UserDetails ud ? ud.getUsername() : String.valueOf(auth.getPrincipal());
        log.info("Authentication success for user '{}', authorities={}", principal, auth.getAuthorities());

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, payload, "Login successful"));
    }

    @Operation(summary = "Register a user (READER/AUTHOR)")
    @PostMapping("/auth/register")
    public ResponseEntity<ApiResponse<Object>> register(@Valid @RequestBody AuthDtos.RegisterRequest request) {
        User user = new User(request.name(), request.email(), request.password(), request.role());
        userService.registerUser(user);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.CREATED, null, "User registered successfully"));
    }

    @Operation(summary = "Logout (revoke current token)")
    @PostMapping("/auth/logout")
    public ResponseEntity<ApiResponse<Object>> logout(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken tokenAuth) {
            Jwt jwt = tokenAuth.getToken();
            String jti = jwt.getId();
            Instant expiresAt = jwt.getExpiresAt();
            if (jti != null && expiresAt != null) {
                tokenBlacklist.revoke(jti, expiresAt);
                log.info("JWT revoked: jti='{}', subject='{}', expiresAt={}", jti, jwt.getSubject(), expiresAt);
            }
        }
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, null, "Logout successful"));
    }
}

