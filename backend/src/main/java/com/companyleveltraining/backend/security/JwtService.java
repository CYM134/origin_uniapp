package com.companyleveltraining.backend.security;

import com.companyleveltraining.backend.auth.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationSeconds;

    public JwtService(
        @Value("${security.jwt.secret}") String secret,
        @Value("${security.jwt.expiration-seconds}") long expirationSeconds
    ) {
        this.signingKey = buildSigningKey(secret);
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(UserAccount user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(expirationSeconds);

        return Jwts.builder()
            .subject(String.valueOf(user.id()))
            .claim("accountNo", user.accountNo())
            .claim("role", user.role())
            .claim("realName", user.realName())
            .claim("gender", user.gender())
            .claim("phone", user.phone())
            .claim("email", user.email())
            .claim("status", user.status())
            .claim("college", user.college())
            .claim("major", user.major())
            .claim("department", user.department())
            .claim("positionTitle", user.positionTitle())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .signWith(signingKey)
            .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public SecurityUser parseToken(String token) {
        Claims claims = parseClaims(token);
        return new SecurityUser(
            Long.parseLong(claims.getSubject()),
            claims.get("accountNo", String.class),
            claims.get("role", String.class),
            claims.get("realName", String.class),
            claims.get("gender", String.class),
            claims.get("phone", String.class),
            claims.get("email", String.class),
            claims.get("status", String.class),
            claims.get("college", String.class),
            claims.get("major", String.class),
            claims.get("department", String.class),
            claims.get("positionTitle", String.class)
        );
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private SecretKey buildSigningKey(String secret) {
        try {
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        } catch (Exception ex) {
            return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }
    }
}
