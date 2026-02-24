package com.blogging_platform.security.auth;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import com.blogging_platform.model.User;
import com.blogging_platform.repository.UserRepository;

/**
 * Custom OAuth2 user service for Google login.
 * 
 * <p>
 * Fetches user details from Google and persists them to the database.
 * If a user with the same email already exists, reuses the existing user.
 * New users are assigned the default READER role.
 * </p>
 */
@Service
public class GoogleOAuth2UserService extends OidcUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public GoogleOAuth2UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            // Delegate to default OIDC user service to load user from Google
            OidcUser oidcUser = super.loadUser(userRequest);

            // Extract user information from Google
            String rawEmail = oidcUser.getEmail();
            String name = oidcUser.getFullName();

            if (rawEmail == null || rawEmail.isBlank()) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error("invalid_request", "Email not provided by Google", null));
            }

            // Normalize email to lowercase to prevent casing-related issues
            String email = rawEmail.trim().toLowerCase();
            System.out.println("[GoogleOAuth2UserService] Processing OAuth2 login for email: " + email + " (original: "
                    + rawEmail + ")");

            // Check if user already exists in database (handle race: two callbacks may run
            // concurrently)
            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> {
                        System.out.println("[GoogleOAuth2UserService] User not found for email: " + email
                                + ". Attempting to create.");
                        // Double-check again within the same lock context would be better, but let's at
                        // least keep this
                        return userRepository.findByEmail(email)
                                .orElseGet(() -> {
                                    System.out
                                            .println("[GoogleOAuth2UserService] Creating new user for email: " + email);
                                    return createAndSaveOAuth2User(email, name);
                                });
                    });

            System.out.println(
                    "[GoogleOAuth2UserService] User resolved: " + user.getEmail() + ", role: " + user.getRole());

            // Build authorities from the user's role
            Collection<? extends GrantedAuthority> authorities = buildAuthorities(user.getRole());

            // Return OidcUser with normalized authorities
            return new DefaultOidcUser(
                    authorities,
                    oidcUser.getIdToken(),
                    oidcUser.getUserInfo());
        } catch (OAuth2AuthenticationException e) {
            System.err.println("[GoogleOAuth2UserService] OAuth2 error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            System.err.println("[GoogleOAuth2UserService] Unexpected error: " + e.getMessage());
            e.printStackTrace();
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("server_error", "Failed to process OAuth2 user: " + e.getMessage(), null),
                    e);
        }
    }

    /**
     * Creates and persists a new user for OAuth2 login.
     * Does not set ID manually – lets JPA generate it to avoid merge/optimistic
     * locking issues.
     */
    private User createAndSaveOAuth2User(String email, String name) {
        User newUser = new User();
        newUser.setName(name != null ? name : email.split("@")[0]);
        newUser.setEmail(email.toLowerCase()); // Ensure lowercase
        String randomPassword = java.util.UUID.randomUUID().toString();
        newUser.setPassword(passwordEncoder.encode(randomPassword));
        newUser.setRole("Regular");
        return userRepository.save(newUser);
    }

    /**
     * Builds Spring Security authorities from the user's role.
     * Normalizes role to ROLE_* format expected by Spring Security.
     */
    private Collection<? extends GrantedAuthority> buildAuthorities(String role) {
        String normalizedRole = normalizeRole(role);
        return java.util.List.of(new SimpleGrantedAuthority("ROLE_" + normalizedRole));
    }

    /**
     * Normalizes role string to uppercase format.
     * Defaults to READER if role is null or unrecognized.
     */
    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "READER";
        }
        // Back-compat with previous schema values
        if ("Admin".equalsIgnoreCase(role)) {
            return "ADMIN";
        }
        if ("Author".equalsIgnoreCase(role)) {
            return "AUTHOR";
        }
        if ("Regular".equalsIgnoreCase(role) || "User".equalsIgnoreCase(role)) {
            return "READER";
        }
        return role.trim().toUpperCase();
    }
}
