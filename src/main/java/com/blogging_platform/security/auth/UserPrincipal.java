package com.blogging_platform.security.auth;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.blogging_platform.model.User;

/**
 * Spring Security adapter for the application's {@link User} entity.
 */
public class UserPrincipal implements UserDetails {

    private final UUID id;
    private final String name;
    private final String email;
    private final String passwordHash;
    private final String role;

    public UserPrincipal(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.passwordHash = user.getPassword();
        this.role = user.getRole();
    }

    public UUID getId() {
        return id;
    }

    public String getDisplayName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Normalize to ROLE_* convention expected by Spring Security.
        String normalized = normalizeRole(role);
        return List.of(new SimpleGrantedAuthority("ROLE_" + normalized));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        // Use email as the principal username
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    private static String normalizeRole(String role) {
        if (role == null) {
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

