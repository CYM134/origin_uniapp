package com.companyleveltraining.backend.common.cache;

import java.time.Duration;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class RedisCacheService {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheService.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final boolean enabled;

    public RedisCacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper,
                             @Value("${cache.redis.enabled:true}") boolean enabled) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.enabled = enabled;
    }

    public <T> T getOrLoad(String key, Duration ttl, Supplier<T> loader, TypeReference<T> type) {
        if (!enabled || ttl == null || ttl.isZero() || ttl.isNegative()) {
            return loader.get();
        }

        String cached = read(key);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, type);
            } catch (Exception ex) {
                log.debug("Redis cache parse failed for key {}: {}", key, ex.getMessage());
            }
        }

        T value = loader.get();
        write(key, value, ttl);
        return value;
    }

    private String read(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception ex) {
            log.debug("Redis cache read failed for key {}: {}", key, ex.getMessage());
            return null;
        }
    }

    private void write(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl);
        } catch (Exception ex) {
            log.debug("Redis cache write failed for key {}: {}", key, ex.getMessage());
        }
    }
}
