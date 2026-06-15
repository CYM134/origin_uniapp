package com.companyleveltraining.backend.dashboard;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.companyleveltraining.backend.common.SecurityUtils;

/**
 * 管理员工作台汇总接口，对应 admin-work 的待办计数（可选增强）。
 */
@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final JdbcTemplate jdbcTemplate;

    public AdminDashboardController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        SecurityUtils.requireRole("admin");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pendingReservations", count("""
            SELECT COUNT(*) FROM reservation_applications
            WHERE deleted_at IS NULL AND (
              (applicant_role = 'student' AND status = 'teacher_approved')
              OR (applicant_role = 'teacher' AND status = 'pending'))
            """));
        result.put("pendingTeacherReviews", count("""
            SELECT COUNT(*) FROM reservation_applications
            WHERE deleted_at IS NULL AND applicant_role = 'student' AND status = 'pending'
            """));
        result.put("pendingTeacherRegistrations", count("""
            SELECT COUNT(*) FROM teacher_registration_applications WHERE status = 'pending'
            """));
        result.put("totalLabs", count("SELECT COUNT(*) FROM laboratories WHERE deleted_at IS NULL"));
        result.put("totalUsers", count("SELECT COUNT(*) FROM sys_users WHERE deleted_at IS NULL AND status <> 'deleted'"));
        return result;
    }

    private long count(String sql) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class);
        return value == null ? 0L : value;
    }
}
