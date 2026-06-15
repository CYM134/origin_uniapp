package com.companyleveltraining.backend.profile;

import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.companyleveltraining.backend.common.BusinessException;
import com.companyleveltraining.backend.profile.dto.ProfileUpdateRequest;

/**
 * 学生/教师个人资料读取与更新。返回对象字段对齐前端 studentInfo / teacherInfo。
 */
@Service
public class ProfileService {

    private final JdbcTemplate jdbcTemplate;

    public ProfileService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> getStudentProfile(Long userId) {
        String sql = """
            SELECT su.real_name AS name, su.account_no AS studentId, su.gender, su.phone, su.email,
                   su.avatar_url AS avatar, su.created_at AS registerTime, su.status,
                   sp.college, sp.major
            FROM sys_users su
            LEFT JOIN student_profiles sp ON sp.user_id = su.id
            WHERE su.id = ? AND su.deleted_at IS NULL
            """;
        return jdbcTemplate.queryForList(sql, userId).stream().findFirst()
            .orElseThrow(() -> BusinessException.notFound("学生信息不存在"));
    }

    public Map<String, Object> getTeacherProfile(Long userId) {
        String sql = """
            SELECT su.real_name AS name, su.account_no AS teacherId, su.gender, su.phone, su.email,
                   su.avatar_url AS avatar, su.created_at AS registerTime, su.status,
                   tp.college, tp.department, tp.position_title AS positionTitle
            FROM sys_users su
            LEFT JOIN teacher_profiles tp ON tp.user_id = su.id
            WHERE su.id = ? AND su.deleted_at IS NULL
            """;
        return jdbcTemplate.queryForList(sql, userId).stream().findFirst()
            .orElseThrow(() -> BusinessException.notFound("教师信息不存在"));
    }

    @Transactional
    public Map<String, Object> updateStudentProfile(Long userId, ProfileUpdateRequest req) {
        jdbcTemplate.update("""
            UPDATE sys_users
            SET real_name = COALESCE(?, real_name), gender = COALESCE(?, gender),
                phone = COALESCE(?, phone), email = COALESCE(?, email)
            WHERE id = ?
            """, req.name(), normalizeGender(req.gender()), req.phone(), req.email(), userId);
        jdbcTemplate.update("""
            UPDATE student_profiles
            SET college = COALESCE(?, college), major = COALESCE(?, major)
            WHERE user_id = ?
            """, req.college(), req.major(), userId);
        return getStudentProfile(userId);
    }

    @Transactional
    public Map<String, Object> updateTeacherProfile(Long userId, ProfileUpdateRequest req) {
        jdbcTemplate.update("""
            UPDATE sys_users
            SET real_name = COALESCE(?, real_name), gender = COALESCE(?, gender),
                phone = COALESCE(?, phone), email = COALESCE(?, email)
            WHERE id = ?
            """, req.name(), normalizeGender(req.gender()), req.phone(), req.email(), userId);
        jdbcTemplate.update("""
            UPDATE teacher_profiles
            SET college = COALESCE(?, college), department = COALESCE(?, department),
                position_title = COALESCE(?, position_title)
            WHERE user_id = ?
            """, req.college(), req.department(), req.positionTitle(), userId);
        return getTeacherProfile(userId);
    }

    public void updateAvatar(Long userId, String avatarUrl) {
        jdbcTemplate.update("UPDATE sys_users SET avatar_url = ? WHERE id = ?", avatarUrl, userId);
    }

    private static String normalizeGender(String gender) {
        if (gender == null || gender.isBlank()) {
            return null;
        }
        if ("male".equals(gender) || "男".equals(gender)) {
            return "male";
        }
        if ("female".equals(gender) || "女".equals(gender)) {
            return "female";
        }
        return "unknown";
    }
}
