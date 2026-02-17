package com.blogging_platform.security.auth;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.blogging_platform.security.jwt.JwtProperties;

/**
 * Issues signed JWTs for authenticated users.
 */
@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties props;

    public JwtService(JwtEncoder jwtEncoder, JwtProperties props) {
        this.jwtEncoder = jwtEncoder;
        this.props = props;
    }

    public String generateToken(Authentication authentication) {
        Instant now = Instant.now();
        Instant expiry = now.plus(props.getTtl());

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(props.getIssuer())
                .issuedAt(now)
                .expiresAt(expiry)
                .subject(authentication.getName())
                .id(UUID.randomUUID().toString()) // jti
                .claim("roles", roles)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long getExpiresInSeconds() {
        return props.getTtl().toSeconds();
    }
}

