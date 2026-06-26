package com.companyleveltraining.backend.security;

import com.companyleveltraining.backend.auth.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    /** HS256 要求密钥至少 256bit（32 字节）。 */
    private static final int MIN_HS256_KEY_BYTES = 32;

    private final SecretKey signingKey;
    private final long expirationSeconds;

    public JwtService(
        @Value("${security.jwt.secret}") String secret,
        @Value("${security.jwt.expiration-seconds}") long expirationSeconds
    ) {
        this.signingKey = buildSigningKey(secret);
        this.expirationSeconds = expirationSeconds;
    }

    public GeneratedToken generateToken(UserAccount user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(expirationSeconds);
        String tokenId = UUID.randomUUID().toString();

        String accessToken = Jwts.builder()
            .id(tokenId)
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
        return new GeneratedToken(accessToken, tokenId);
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
            claims.get("positionTitle", String.class),
            claims.getId()
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
        if (secret == null || secret.isBlank()) {
            log.warn("未配置 JWT_SECRET，使用临时随机密钥（重启后既有 token 失效）");
            // Keys.secretKeyFor 内部使用 SecureRandom 生成一个安全的 256bit HS256 密钥
            return Keys.secretKeyFor(SignatureAlgorithm.HS256);
        }
        byte[] keyBytes = decodeSecret(secret);
        if (keyBytes.length < MIN_HS256_KEY_BYTES) {
            log.warn(
                "配置的 JWT_SECRET 长度不足（{} 字节 < {} 字节），HS256 要求至少 256bit，使用临时随机密钥（重启后既有 token 失效）",
                keyBytes.length, MIN_HS256_KEY_BYTES
            );
            return Keys.secretKeyFor(SignatureAlgorithm.HS256);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private byte[] decodeSecret(String secret) {
        try {
            return Decoders.BASE64.decode(secret);
        } catch (Exception ex) {
            return secret.getBytes(StandardCharsets.UTF_8);
        }
    }

    public record GeneratedToken(String accessToken, String tokenId) {
    }
}
