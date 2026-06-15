package com.companyleveltraining.backend.auth;

import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.companyleveltraining.backend.auth.dto.StudentRegisterRequest;
import com.companyleveltraining.backend.auth.dto.TeacherRegisterRequest;

/**
 * 账号与注册相关的写操作仓储：注册查重、创建学生账号+资料、写入教师注册申请、改密。
 */
@Repository
public class AccountRepository {

    private final JdbcTemplate jdbcTemplate;

    public AccountRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existsAccount(String role, String accountNo) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM sys_users WHERE role = ? AND account_no = ? AND deleted_at IS NULL",
            Integer.class, role, accountNo);
        return count != null && count > 0;
    }

    public boolean existsPendingTeacherApplication(String teacherNo) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM teacher_registration_applications WHERE teacher_no = ? AND status = 'pending'",
            Integer.class, teacherNo);
        return count != null && count > 0;
    }

    public Long insertStudentUser(StudentRegisterRequest req, String passwordHash) {
        String sql = """
            INSERT INTO sys_users (account_no, role, password_hash, real_name, gender, phone, status, agreed_terms_at)
            VALUES (?, 'student', ?, ?, ?, ?, 'active', CURRENT_TIMESTAMP(3))
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[] {"id"});
            ps.setString(1, req.studentId());
            ps.setString(2, passwordHash);
            ps.setString(3, req.name());
            ps.setString(4, normalizeGender(req.gender()));
            ps.setString(5, req.phone());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public void insertStudentProfile(Long userId, StudentRegisterRequest req) {
        jdbcTemplate.update("""
            INSERT INTO student_profiles (user_id, student_no, college, major)
            VALUES (?, ?, ?, ?)
            """, userId, req.studentId(), req.college(), req.major());
    }

    public Long insertTeacherRegistration(TeacherRegisterRequest req, String applicationNo, String passwordHash) {
        String sql = """
            INSERT INTO teacher_registration_applications
              (application_no, teacher_no, real_name, gender, college, department, position_title,
               phone, email, password_hash, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'pending')
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[] {"id"});
            ps.setString(1, applicationNo);
            ps.setString(2, req.teacherId());
            ps.setString(3, req.name());
            ps.setString(4, normalizeGender(req.gender()));
            ps.setString(5, req.college());
            ps.setString(6, req.department());
            ps.setString(7, req.positionTitle());
            ps.setString(8, req.phone());
            ps.setString(9, req.email());
            ps.setString(10, passwordHash);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public Optional<String> findPasswordHashById(Long userId) {
        return jdbcTemplate.query(
            "SELECT password_hash FROM sys_users WHERE id = ? AND deleted_at IS NULL",
            (rs, rowNum) -> rs.getString("password_hash"), userId).stream().findFirst();
    }

    public void updatePassword(Long userId, String passwordHash) {
        jdbcTemplate.update(
            "UPDATE sys_users SET password_hash = ? WHERE id = ?", passwordHash, userId);
    }

    private static String normalizeGender(String gender) {
        if ("male".equals(gender) || "男".equals(gender)) {
            return "male";
        }
        if ("female".equals(gender) || "女".equals(gender)) {
            return "female";
        }
        return "unknown";
    }
}
