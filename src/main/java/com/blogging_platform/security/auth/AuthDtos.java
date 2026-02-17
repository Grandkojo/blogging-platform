package com.blogging_platform.security.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTOs for authentication endpoints.
 */
public final class AuthDtos {

    private AuthDtos() {}

    public record LoginRequest(
            @NotBlank @Email @Schema(example = "user@example.com") String email,
            @NotBlank @Schema(example = "password123") String password
    ) {}

    public record RegisterRequest(
            @NotBlank @Schema(example = "Jane Doe") String name,
            @NotBlank @Email @Schema(example = "jane@example.com") String email,
            @NotBlank @Size(min = 8) @Schema(example = "password123") String password,
            @NotBlank @Schema(example = "READER") String role
    ) {}

    public record AuthResponse(
            @Schema(description = "JWT access token") String accessToken,
            @Schema(description = "Token type") String tokenType,
            @Schema(description = "Seconds until expiration") long expiresInSeconds
    ) {}
}

