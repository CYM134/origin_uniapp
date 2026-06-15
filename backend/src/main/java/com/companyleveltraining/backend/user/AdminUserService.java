package com.companyleveltraining.backend.user;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.companyleveltraining.backend.audit.AuditLogService;
import com.companyleveltraining.backend.common.BusinessException;

/**
 * 管理员用户管理业务，对应 admin-system-management 的「用户管理」Tab：
 * 列表/搜索/筛选、新增、编辑、启用禁用、重置密码、删除。
 */
@Service
public class AdminUserService {

    private static final String DEFAULT_PASSWORD = "123456";

    private static final String BASE_SELECT = """
        SELECT id, real_name AS name, account_no AS accountNo, avatar_url AS avatar,
               role, gender, phone, email, status,
               CASE role WHEN 'admin' THEN '管理员' WHEN 'teacher' THEN '教师' WHEN 'student' THEN '学生' ELSE role END AS roleName,
               last_login_at AS lastLogin, login_count AS loginCount, created_at AS createTime
        FROM sys_users
        WHERE deleted_at IS NULL AND status <> 'deleted'
        """;

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public AdminUserService(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder,
                            AuditLogService auditLogService) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    public List<Map<String, Object>> list(String keyword, String role) {
        StringBuilder sql = new StringBuilder(BASE_SELECT);
        java.util.List<Object> args = new java.util.ArrayList<>();
        if (role != null && !role.isBlank() && !"all".equals(role)) {
            sql.append(" AND role = ? ");
            args.add(role);
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (real_name LIKE ? OR account_no LIKE ? OR phone LIKE ?) ");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
            args.add(like);
        }
        sql.append(" ORDER BY id ASC ");
        return jdbcTemplate.queryForList(sql.toString(), args.toArray());
    }

    public Map<String, Object> create(Map<String, Object> body, Long operatorId) {
        String role = str(body.get("role"));
        String name = str(body.get("name"));
        if (role == null || name == null) {
            throw BusinessException.badRequest("姓名和角色不能为空");
        }
        String accountNo = str(body.get("accountNo"));
        if (accountNo == null || accountNo.isBlank()) {
            accountNo = str(body.get("studentId"));
        }
        if (accountNo == null || accountNo.isBlank()) {
            accountNo = str(body.get("teacherId"));
        }
        if (accountNo == null || accountNo.isBlank()) {
            // 前端添加用户表单未提供账号时，按角色前缀自动生成一个，保证可用
            String prefix = switch (role) {
                case "student" -> "S";
                case "teacher" -> "T";
                default -> "U";
            };
            accountNo = prefix + System.currentTimeMillis();
        }
        Integer exists = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM sys_users WHERE role = ? AND account_no = ? AND deleted_at IS NULL",
            Integer.class, role, accountNo);
        if (exists != null && exists > 0) {
            throw BusinessException.conflict("该角色下账号已存在");
        }
        String rawPassword = str(body.get("password"));
        String hash = passwordEncoder.encode(rawPassword == null || rawPassword.isBlank() ? DEFAULT_PASSWORD : rawPassword);
        final String finalAccountNo = accountNo;
        org.springframework.jdbc.support.KeyHolder keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement("""
                INSERT INTO sys_users (account_no, role, password_hash, real_name, phone, email, status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, new String[] {"id"});
            ps.setString(1, finalAccountNo);
            ps.setString(2, role);
            ps.setString(3, hash);
            ps.setString(4, name);
            ps.setString(5, str(body.get("phone")));
            ps.setString(6, str(body.get("email")));
            ps.setString(7, statusOrDefault(str(body.get("status"))));
            return ps;
        }, keyHolder);
        Long id = keyHolder.getKey().longValue();
        auditLogService.record(operatorId, "admin", "user", "create", "sys_users", id, null);
        return getById(id);
    }

    public Map<String, Object> update(Long id, Map<String, Object> body, Long operatorId) {
        requireExists(id);
        jdbcTemplate.update("""
            UPDATE sys_users SET
              real_name = COALESCE(?, real_name),
              role = COALESCE(?, role),
              status = COALESCE(?, status),
              phone = COALESCE(?, phone),
              email = COALESCE(?, email)
            WHERE id = ?
            """, str(body.get("name")), str(body.get("role")), str(body.get("status")),
            str(body.get("phone")), str(body.get("email")), id);
        auditLogService.record(operatorId, "admin", "user", "update", "sys_users", id, null);
        return getById(id);
    }

    public Map<String, Object> toggleStatus(Long id, String status, Long operatorId) {
        Map<String, Object> user = requireExists(id);
        String target = status;
        if (target == null || target.isBlank()) {
            target = "active".equals(user.get("status")) ? "disabled" : "active";
        }
        jdbcTemplate.update("UPDATE sys_users SET status = ? WHERE id = ?", target, id);
        auditLogService.record(operatorId, "admin", "user", "toggle_status", "sys_users", id, null);
        return getById(id);
    }

    public void resetPassword(Long id, Long operatorId) {
        requireExists(id);
        jdbcTemplate.update("UPDATE sys_users SET password_hash = ? WHERE id = ?",
            passwordEncoder.encode(DEFAULT_PASSWORD), id);
        auditLogService.record(operatorId, "admin", "user", "reset_password", "sys_users", id, null);
    }

    public void delete(Long id, Long operatorId) {
        requireExists(id);
        jdbcTemplate.update("""
            UPDATE sys_users SET status = 'deleted', deleted_at = CURRENT_TIMESTAMP(3) WHERE id = ?
            """, id);
        auditLogService.record(operatorId, "admin", "user", "delete", "sys_users", id, null);
    }

    private Map<String, Object> getById(Long id) {
        return jdbcTemplate.queryForList(BASE_SELECT + " AND id = ? LIMIT 1", id).stream().findFirst()
            .orElseThrow(() -> BusinessException.notFound("用户不存在"));
    }

    private Map<String, Object> requireExists(Long id) {
        return jdbcTemplate.queryForList(
            "SELECT id, status FROM sys_users WHERE id = ? AND deleted_at IS NULL", id)
            .stream().findFirst()
            .orElseThrow(() -> BusinessException.notFound("用户不存在"));
    }

    private String statusOrDefault(String status) {
        return status == null || status.isBlank() ? "active" : status;
    }

    private String str(Object v) {
        return v == null ? null : String.valueOf(v);
    }
}
