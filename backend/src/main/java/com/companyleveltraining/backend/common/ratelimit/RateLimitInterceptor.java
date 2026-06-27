package com.companyleveltraining.backend.common.ratelimit;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.companyleveltraining.backend.security.SecurityUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RedisRateLimitService rateLimitService;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final List<RateLimitRule> rules;

    public RateLimitInterceptor(RedisRateLimitService rateLimitService,
                                RateLimitProperties properties,
                                ObjectMapper objectMapper) {
        this.rateLimitService = rateLimitService;
        this.objectMapper = objectMapper;
        this.rules = List.of(
            new RateLimitRule("auth-login", "POST", "/api/auth/login",
                properties.loginPerMinute(), Duration.ofMinutes(1), RateLimitRule.Subject.IP),
            new RateLimitRule("auth-register", "POST", "/api/auth/*/register",
                properties.registerPerFiveMinutes(), Duration.ofMinutes(5), RateLimitRule.Subject.IP),
            new RateLimitRule("ai-chat", "POST", "/api/ai/chat",
                properties.aiPerMinute(), Duration.ofMinutes(1), RateLimitRule.Subject.USER_OR_IP)
        );
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws IOException {
        RateLimitRule rule = findRule(request);
        if (rule == null) {
            return true;
        }

        String subject = subject(request, rule);
        RateLimitResult result = rateLimitService.check(rule.id(), subject, rule.limit(), rule.window());
        response.setHeader("X-RateLimit-Limit", String.valueOf(result.limit()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(result.remaining()));

        if (result.allowed()) {
            return true;
        }

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(result.retryAfterSeconds()));
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), Map.of(
            "code", HttpStatus.TOO_MANY_REQUESTS.value(),
            "message", "请求过于频繁，请稍后再试",
            "retryAfterSeconds", result.retryAfterSeconds()
        ));
        return false;
    }

    private RateLimitRule findRule(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        return rules.stream()
            .filter(rule -> rule.method().equalsIgnoreCase(method))
            .filter(rule -> pathMatcher.match(rule.pathPattern(), path))
            .findFirst()
            .orElse(null);
    }

    private String subject(HttpServletRequest request, RateLimitRule rule) {
        if (rule.subject() == RateLimitRule.Subject.USER_OR_IP) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof SecurityUser user) {
                return "user:" + user.id();
            }
        }
        return "ip:" + clientIp(request);
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
