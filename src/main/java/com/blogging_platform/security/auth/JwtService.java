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

        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .issuer(props.getIssuer())
                .issuedAt(now)
                .expiresAt(expiry)
                .subject(authentication.getName())
                .id(UUID.randomUUID().toString()) // jti
                .claim("roles", roles);

        // Embed the user's UUID so controllers can derive it from the token
        // instead of accepting it from untrusted request input.
        if (authentication.getPrincipal() instanceof UserPrincipal up) {
            claimsBuilder.claim("userId", up.getId().toString());
        }

        JwtClaimsSet claims = claimsBuilder.build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long getExpiresInSeconds() {
        return props.getTtl().toSeconds();
    }
}

