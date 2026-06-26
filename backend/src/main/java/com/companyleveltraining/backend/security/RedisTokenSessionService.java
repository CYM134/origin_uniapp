package com.companyleveltraining.backend.security;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.companyleveltraining.backend.auth.UserAccount;
import com.companyleveltraining.backend.common.BusinessException;

@Service
public class RedisTokenSessionService {

    private static final Logger log = LoggerFactory.getLogger(RedisTokenSessionService.class);

    private static final String TOKEN_PREFIX = "auth:token:";
    private static final String USER_TOKENS_PREFIX = "auth:user:";

    private final StringRedisTemplate redisTemplate;
    private final boolean enabled;

    public RedisTokenSessionService(StringRedisTemplate redisTemplate,
                                    @Value("${security.redis-session.enabled:true}") boolean enabled) {
        this.redisTemplate = redisTemplate;
        this.enabled = enabled;
    }

    public boolean enabled() {
        return enabled;
    }

    public void createSession(UserAccount user, String tokenId, long ttlSeconds) {
        if (!enabled) {
            return;
        }
        if (tokenId == null || tokenId.isBlank()) {
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, "登录会话创建失败，请稍后重试");
        }
        try {
            Duration ttl = Duration.ofSeconds(Math.max(1, ttlSeconds));
            String value = user.id() + "|" + user.role() + "|" + user.accountNo();
            redisTemplate.opsForValue().set(tokenKey(tokenId), value, ttl);
            redisTemplate.opsForSet().add(userTokensKey(user.id()), tokenId);
            redisTemplate.expire(userTokensKey(user.id()), ttl);
        } catch (Exception ex) {
            log.warn("Create Redis token session failed for user {}: {}", user.id(), ex.getMessage());
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, "登录会话创建失败，请确认 Redis 可用");
        }
    }

    public boolean isSessionActive(SecurityUser user) {
        if (!enabled) {
            return true;
        }
        if (user == null || user.id() == null || user.tokenId() == null || user.tokenId().isBlank()) {
            return false;
        }
        try {
            String value = redisTemplate.opsForValue().get(tokenKey(user.tokenId()));
            String expectedPrefix = user.id() + "|" + user.role() + "|";
            return value != null && value.startsWith(expectedPrefix);
        } catch (Exception ex) {
            log.warn("Read Redis token session failed for user {}: {}", user.id(), ex.getMessage());
            return false;
        }
    }

    public void revokeSession(SecurityUser user) {
        if (!enabled || user == null || user.tokenId() == null || user.tokenId().isBlank()) {
            return;
        }
        try {
            redisTemplate.delete(tokenKey(user.tokenId()));
            if (user.id() != null) {
                redisTemplate.opsForSet().remove(userTokensKey(user.id()), user.tokenId());
            }
        } catch (Exception ex) {
            log.warn("Revoke Redis token session failed for user {}: {}", user.id(), ex.getMessage());
        }
    }

    public void revokeAllSessions(Long userId) {
        if (!enabled || userId == null) {
            return;
        }
        try {
            String userKey = userTokensKey(userId);
            Set<String> tokenIds = redisTemplate.opsForSet().members(userKey);
            if (tokenIds != null && !tokenIds.isEmpty()) {
                List<String> keys = new ArrayList<>();
                for (String tokenId : tokenIds) {
                    keys.add(tokenKey(tokenId));
                }
                redisTemplate.delete(keys);
            }
            redisTemplate.delete(userKey);
        } catch (Exception ex) {
            log.warn("Revoke all Redis token sessions failed for user {}: {}", userId, ex.getMessage());
        }
    }

    private String tokenKey(String tokenId) {
        return TOKEN_PREFIX + tokenId;
    }

    private String userTokensKey(Long userId) {
        return USER_TOKENS_PREFIX + userId + ":tokens";
    }
}
