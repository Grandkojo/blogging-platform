package com.blogging_platform.security.jwt;

import java.time.Instant;

/**
 * Stores revoked JWT identifiers (jti) until their natural expiration.
 */
public interface TokenBlacklist {

    /**
     * Marks the given token id as revoked until the provided expiry time.
     *
     * @param jti token id
     * @param expiresAt token expiry (used for cleanup/TTL)
     */
    void revoke(String jti, Instant expiresAt);

    /**
     * Returns true if the token id has been revoked.
     */
    boolean isRevoked(String jti);
}

