package com.blogging_platform.security.jwt;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * In-memory blacklist for revoked JWT ids (jti).
 *
 * <p>This will be replaced/enhanced in the DSA epic (hashing + cleanup + caching policies).</p>
 */
@Component
public class InMemoryTokenBlacklist implements TokenBlacklist {

    private static final Logger log = LoggerFactory.getLogger(InMemoryTokenBlacklist.class);

    private final Map<String, Instant> revoked = new ConcurrentHashMap<>();

    @Override
    public void revoke(String jti, Instant expiresAt) {
        if (jti == null || jti.isBlank() || expiresAt == null) {
            return;
        }
        revoked.put(jti, expiresAt);
        log.debug("Token revoked: jti='{}', expiresAt={}", jti, expiresAt);
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
        log.trace("Token jti='{}' is currently revoked (expiresAt={})", jti, expiresAt);
        return true;
    }
}

