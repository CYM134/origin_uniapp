package com.companyleveltraining.backend.common.ratelimit;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisRateLimitService {

    private static final Logger log = LoggerFactory.getLogger(RedisRateLimitService.class);

    private final StringRedisTemplate redisTemplate;
    private final RateLimitProperties properties;

    public RedisRateLimitService(StringRedisTemplate redisTemplate, RateLimitProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    public RateLimitResult check(String ruleId, String subject, int limit, Duration window) {
        if (!properties.enabled() || limit <= 0 || window == null || window.isZero() || window.isNegative()) {
            return new RateLimitResult(true, Math.max(limit, 0), Math.max(limit, 0), 0);
        }

        String key = "ratelimit:%s:%d:%s".formatted(ruleId, window.toSeconds(), sanitize(subject));
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1) {
                redisTemplate.expire(key, window);
            }

            long current = count == null ? 0 : count;
            long remaining = Math.max(0, limit - current);
            long ttl = redisTemplate.getExpire(key);
            long retryAfter = ttl < 0 ? window.toSeconds() : ttl;
            return new RateLimitResult(current <= limit, limit, remaining, retryAfter);
        } catch (Exception ex) {
            log.debug("Redis rate limit check failed for rule {} and subject {}: {}", ruleId, subject, ex.getMessage());
            return new RateLimitResult(true, limit, limit, 0);
        }
    }

    private String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        return value.replaceAll("[^a-zA-Z0-9_.:-]", "_");
    }
}
