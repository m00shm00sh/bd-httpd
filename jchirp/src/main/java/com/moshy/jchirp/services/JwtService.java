package com.moshy.jchirp.services;

import com.moshy.jchirp.util.ThrowableToOptional;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtService {
    private final SecretKey secret;

    private final String issuer;

    public JwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.issuer}") String issuer) {
        this.issuer = issuer;
        this.secret = getSignKey(secret);
    }

    public String generateToken(UUID uid) {
        var now = Instant.now();
        var exp = now.plus(1, ChronoUnit.HOURS);

        return Jwts.builder()
            .subject(uid.toString())
            .issuer(issuer)
            .issuedAt(Date.from(now))
            .expiration(Date.from(exp))
            .signWith(secret)
            .compact();
    }

    private SecretKey getSignKey(String fromConfig) {
        if (fromConfig.length() < 32)
            return Jwts.SIG.HS256.key().build();
        byte[] bytes = fromConfig.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(bytes);
    }

    private Claims extractClaims(String jwt) {
        return Jwts.parser()
            .verifyWith(secret)
            .build()
            .parseSignedClaims(jwt)
            .getPayload();
    }

    public @Nullable String getUserFromToken(@Nonnull String token) {
        String[] sub = { null };
        ThrowableToOptional.orEmpty(() -> extractClaims(token), JwtException.class)
            .ifPresent(body -> sub[0] = body.getSubject());
        return sub[0];
    }
}

