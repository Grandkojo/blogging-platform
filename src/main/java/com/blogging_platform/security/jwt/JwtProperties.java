package com.blogging_platform.security.jwt;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for JWT issuance and validation.
 */
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    /**
     * HMAC secret used to sign JWTs. In production, provide via environment variable
     * and keep it long and random.
     */
    private String secret;

    /**
     * Token time-to-live.
     */
    private Duration ttl = Duration.ofHours(1);

    /**
     * JWT issuer claim.
     */
    private String issuer = "https://blogging-platform";

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Duration getTtl() {
        return ttl;
    }

    public void setTtl(Duration ttl) {
        this.ttl = ttl;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}

