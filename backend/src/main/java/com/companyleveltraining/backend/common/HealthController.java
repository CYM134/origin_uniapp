package com.companyleveltraining.backend.common;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查接口，用于容器编排、部署验证和运行状态探测。
 */
@RestController
@RequestMapping("/api/health")
@Tag(name = "健康检查", description = "服务和数据库健康检查接口")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    @Operation(summary = "服务健康检查", description = "检查后端服务进程是否可用")
    public Map<String, Object> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("service", "backend");
        body.put("timestamp", LocalDateTime.now());
        return body;
    }

    @GetMapping("/db")
    @Operation(summary = "数据库健康检查", description = "检查后端是否可以连接 MySQL 并读取数据库时间")
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
