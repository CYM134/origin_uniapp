package com.companyleveltraining.backend.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * 审计日志写入服务。各业务模块在关键操作（登录、增删改、审批等）后调用，写入 audit_logs 表。
 * 写入失败不影响主流程，仅记录错误日志。
 */
@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final JdbcTemplate jdbcTemplate;

    public AuditLogService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 记录一条审计日志。
     *
     * @param userId     操作人用户 id，可为 null（匿名）
     * @param role       操作人角色：admin/teacher/student/anonymous
     * @param module     业务模块名，例如 laboratory、reservation、user
     * @param action     动作名，例如 create、update、approve
     * @param targetType 目标实体类型，可为 null
     * @param targetId   目标实体 id，可为 null
     * @param detailJson 结构化详情（已是合法 JSON 字符串），可为 null
     */
    public void record(Long userId, String role, String module, String action,
                       String targetType, Long targetId, String detailJson) {
        try {
            String sql = """
                INSERT INTO audit_logs (user_id, role, module, action, target_type, target_id, detail, ip_address, user_agent)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
            jdbcTemplate.update(sql,
                userId,
                role == null ? "anonymous" : role,
                module,
                action,
                targetType,
                targetId,
                detailJson,
                null,
                "backend-api"
            );
        } catch (Exception ex) {
            log.warn("写入审计日志失败 module={}, action={}: {}", module, action, ex.getMessage());
        }
    }
}
