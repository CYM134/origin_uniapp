package com.companyleveltraining.backend.audit;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.companyleveltraining.backend.common.SecurityUtils;

/**
 * 操作日志（审计日志）查询接口，对应前端 logs 页改造后的「系统操作日志」。仅管理员可访问。
 */
@RestController
@RequestMapping("/api/admin/audit-logs")
public class AuditLogController {

    private final JdbcTemplate jdbcTemplate;

    public AuditLogController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public List<Map<String, Object>> list(@RequestParam(required = false) String module,
                                          @RequestParam(defaultValue = "200") int limit) {
        SecurityUtils.requireRole("admin");
        int safeLimit = Math.min(Math.max(limit, 1), 500);
        StringBuilder sql = new StringBuilder("""
            SELECT al.id, al.user_id AS userId, al.role, al.module, al.action,
                   al.target_type AS targetType, al.target_id AS targetId,
                   al.detail, DATE_FORMAT(al.created_at, '%Y-%m-%d %H:%i:%s') AS createTime, su.real_name AS userName
            FROM audit_logs al
            LEFT JOIN sys_users su ON su.id = al.user_id
            """);
        if (module != null && !module.isBlank()) {
            sql.append(" WHERE al.module = ? ");
            sql.append(" ORDER BY al.created_at DESC LIMIT ").append(safeLimit);
            return jdbcTemplate.queryForList(sql.toString(), module);
        }
        sql.append(" ORDER BY al.created_at DESC LIMIT ").append(safeLimit);
        return jdbcTemplate.queryForList(sql.toString());
    }
}
