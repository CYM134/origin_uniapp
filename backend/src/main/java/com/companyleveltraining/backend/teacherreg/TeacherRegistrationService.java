package com.companyleveltraining.backend.teacherreg;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.companyleveltraining.backend.audit.AuditLogService;
import com.companyleveltraining.backend.common.BusinessException;

/**
 * 教师注册申请审核业务，对应 admin-teacher-registration 页。
 * 通过审核时会用申请材料创建正式教师账号（sys_users + teacher_profiles）。
 */
@Service
public class TeacherRegistrationService {

    private static final String BASE_SELECT = """
        SELECT id, application_no AS applicationNo, teacher_no AS teacherNo, teacher_no AS teacherId,
               real_name AS name, gender, college, department, position_title AS position,
               position_title AS positionTitle, phone, email,
               id_card_front_url AS idCardFront, id_card_back_url AS idCardBack,
               teacher_card_image_url AS teacherCardImage,
               status, reject_reason AS rejectReason,
               DATE_FORMAT(applied_at, '%Y-%m-%d %H:%i:%s') AS registerTime,
               DATE_FORMAT(reviewed_at, '%Y-%m-%d %H:%i:%s') AS approvalTime
        FROM teacher_registration_applications
        """;

    private final JdbcTemplate jdbcTemplate;
    private final AuditLogService auditLogService;

    public TeacherRegistrationService(JdbcTemplate jdbcTemplate, AuditLogService auditLogService) {
        this.jdbcTemplate = jdbcTemplate;
        this.auditLogService = auditLogService;
    }

    public List<Map<String, Object>> list(String status) {
        if (status != null && !status.isBlank() && !"all".equals(status)) {
            return jdbcTemplate.queryForList(BASE_SELECT + " WHERE status = ? ORDER BY applied_at DESC", status);
        }
        return jdbcTemplate.queryForList(BASE_SELECT + " ORDER BY applied_at DESC");
    }

    public Map<String, Object> get(Long id) {
        return jdbcTemplate.queryForList(BASE_SELECT + " WHERE id = ? LIMIT 1", id).stream().findFirst()
            .orElseThrow(() -> BusinessException.notFound("注册申请不存在"));
    }

    @Transactional
    public Map<String, Object> approve(Long id, Long reviewerId) {
        Map<String, Object> app = findRawPending(id);
        String teacherNo = (String) app.get("teacher_no");

        Integer exists = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM sys_users WHERE role = 'teacher' AND account_no = ? AND deleted_at IS NULL",
            Integer.class, teacherNo);
        if (exists != null && exists > 0) {
            throw BusinessException.conflict("该工号已存在教师账号");
        }

        org.springframework.jdbc.support.KeyHolder keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement("""
                INSERT INTO sys_users (account_no, role, password_hash, real_name, gender, phone, email, status)
                VALUES (?, 'teacher', ?, ?, ?, ?, ?, 'active')
                """, new String[] {"id"});
            ps.setString(1, teacherNo);
            ps.setString(2, (String) app.get("password_hash"));
            ps.setString(3, (String) app.get("real_name"));
            ps.setString(4, (String) app.get("gender"));
            ps.setString(5, (String) app.get("phone"));
            ps.setString(6, (String) app.get("email"));
            return ps;
        }, keyHolder);
        Long userId = keyHolder.getKey().longValue();

        jdbcTemplate.update("""
            INSERT INTO teacher_profiles (user_id, teacher_no, college, department, position_title)
            VALUES (?, ?, ?, ?, ?)
            """, userId, teacherNo, defaultCollege(app.get("college")), app.get("department"), app.get("position_title"));

        jdbcTemplate.update("""
            UPDATE teacher_registration_applications
            SET status = 'approved', reviewer_user_id = ?, reviewed_at = CURRENT_TIMESTAMP(3)
            WHERE id = ?
            """, reviewerId, id);

        auditLogService.record(reviewerId, "admin", "teacher_registration", "approve",
            "teacher_registration_applications", id, null);
        return get(id);
    }

    @Transactional
    public Map<String, Object> reject(Long id, Long reviewerId, String reason) {
        findRawPending(id);
        if (reason == null || reason.isBlank()) {
            throw BusinessException.badRequest("请填写拒绝原因");
        }
        jdbcTemplate.update("""
            UPDATE teacher_registration_applications
            SET status = 'rejected', reject_reason = ?, reviewer_user_id = ?, reviewed_at = CURRENT_TIMESTAMP(3)
            WHERE id = ?
            """, reason, reviewerId, id);
        auditLogService.record(reviewerId, "admin", "teacher_registration", "reject",
            "teacher_registration_applications", id, null);
        return get(id);
    }

    private Map<String, Object> findRawPending(Long id) {
        Map<String, Object> app = jdbcTemplate.queryForList(
            "SELECT * FROM teacher_registration_applications WHERE id = ? LIMIT 1", id)
            .stream().findFirst()
            .orElseThrow(() -> BusinessException.notFound("注册申请不存在"));
        if (!"pending".equals(app.get("status"))) {
            throw BusinessException.conflict("该申请已审核，不能重复操作");
        }
        return app;
    }

    private String defaultCollege(Object college) {
        return college == null ? "未填写" : String.valueOf(college);
    }
}
