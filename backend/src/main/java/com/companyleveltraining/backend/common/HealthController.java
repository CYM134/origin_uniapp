package com.companyleveltraining.backend.common;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查接口，对应 backend/README.md 中描述的 /api/health 与 /api/health/db。
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public Map<String, Object> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("service", "backend");
        body.put("timestamp", LocalDateTime.now());
        return body;
    }

    @GetMapping("/db")
    public Map<String, Object> db() {
        Map<String, Object> body = new LinkedHashMap<>();
        String database = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        LocalDateTime serverTime = jdbcTemplate.queryForObject("SELECT NOW(3)", LocalDateTime.class);
        body.put("status", "UP");
        body.put("database", database);
        body.put("serverTime", serverTime);
        return body;
    }
}
