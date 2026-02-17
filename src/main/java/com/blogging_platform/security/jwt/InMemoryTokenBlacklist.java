package com.blogging_platform.security.jwt;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * In-memory blacklist for revoked JWT ids (jti).
 *
 * <p>This will be replaced/enhanced in the DSA epic (hashing + cleanup + caching policies).</p>
 */
@Component
public class InMemoryTokenBlacklist implements TokenBlacklist {

    private final Map<String, Instant> revoked = new ConcurrentHashMap<>();

    @Override
    public void revoke(String jti, Instant expiresAt) {
        if (jti == null || jti.isBlank() || expiresAt == null) {
            return;
        }
        revoked.put(jti, expiresAt);
    }

    @Override
    public boolean isRevoked(String jti) {
        if (jti == null || jti.isBlank()) {
            return false;
        }
        Instant expiresAt = revoked.get(jti);
        if (expiresAt == null) {
            return false;
        }
        if (expiresAt.isBefore(Instant.now())) {
            revoked.remove(jti);
            return false;
        }
        return true;
    }
}

