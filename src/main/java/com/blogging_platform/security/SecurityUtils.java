package com.blogging_platform.security;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.blogging_platform.exceptions.AuthorizationException;

/**
 * Utility methods for extracting identity information from the active
 * Spring Security {@link Authentication} principal.
 *
 * <p>Controllers must call {@link #getCurrentUserId} instead of accepting a
 * {@code userId} parameter from the client, which prevents IDOR attacks where
 * an authenticated user supplies another user's id in the request.</p>
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    /**
     * Returns the UUID of the currently authenticated user, read from the
     * {@code userId} claim embedded in the JWT at token-issuance time.
     *
     * @param authentication the active authentication from the SecurityContext
     * @return the authenticated user's UUID
     * @throws AuthorizationException if the authentication object carries no
     *         resolvable {@code userId} claim
     */
    public static UUID getCurrentUserId(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            String userId = jwtAuth.getToken().getClaimAsString("userId");
            if (userId != null) {
                return UUID.fromString(userId);
            }
        }
        throw new AuthorizationException(
                "Cannot resolve authenticated user identity: 'userId' claim missing from token");
    }
}
