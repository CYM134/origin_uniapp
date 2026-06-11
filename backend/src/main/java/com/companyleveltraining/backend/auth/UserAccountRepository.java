package com.companyleveltraining.backend.auth;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class UserAccountRepository {

    private static final Logger log = LoggerFactory.getLogger(UserAccountRepository.class);

    private static final RowMapper<UserAccount> USER_ROW_MAPPER = (rs, rowNum) -> new UserAccount(
        rs.getLong("id"),
        rs.getString("account_no"),
        rs.getString("role"),
        rs.getString("password_hash"),
        rs.getString("real_name"),
        rs.getString("gender"),
        rs.getString("phone"),
        rs.getString("email"),
        rs.getString("status"),
        rs.getString("college"),
        rs.getString("major"),
        rs.getString("department"),
        rs.getString("position_title")
    );

    private final JdbcTemplate jdbcTemplate;

    public UserAccountRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<UserAccount> findActiveByRoleAndAccountNo(String role, String accountNo) {
        long startTime = System.currentTimeMillis();
        String sql = """
            SELECT
              su.id,
              su.account_no,
              su.role,
              su.password_hash,
              su.real_name,
              su.gender,
              su.phone,
              su.email,
              su.status,
              CASE WHEN su.role = 'student' THEN sp.college ELSE tp.college END AS college,
              sp.major AS major,
              tp.department AS department,
              tp.position_title AS position_title
            FROM sys_users su
            LEFT JOIN student_profiles sp
              ON su.role = 'student' AND sp.user_id = su.id
            LEFT JOIN teacher_profiles tp
              ON su.role = 'teacher' AND tp.user_id = su.id
            WHERE su.role = ?
              AND su.account_no = ?
              AND su.status = 'active'
              AND su.deleted_at IS NULL
            LIMIT 1
            """;
        Optional<UserAccount> result = jdbcTemplate.query(sql, USER_ROW_MAPPER, role, accountNo).stream().findFirst();
        log.info(
            "SQL findActiveByRoleAndAccountNo role={}, accountNo={} -> found={} ({} ms)",
            role,
            accountNo,
            result.isPresent(),
            System.currentTimeMillis() - startTime
        );
        return result;
    }

    public Optional<UserAccount> findProfileById(Long userId) {
        long startTime = System.currentTimeMillis();
        String sql = """
            SELECT
              su.id,
              su.account_no,
              su.role,
              su.password_hash,
              su.real_name,
              su.gender,
              su.phone,
              su.email,
              su.status,
              CASE WHEN su.role = 'student' THEN sp.college ELSE tp.college END AS college,
              sp.major AS major,
              tp.department AS department,
              tp.position_title AS position_title
            FROM sys_users su
            LEFT JOIN student_profiles sp
              ON su.role = 'student' AND sp.user_id = su.id
            LEFT JOIN teacher_profiles tp
              ON su.role = 'teacher' AND tp.user_id = su.id
            WHERE su.id = ?
              AND su.deleted_at IS NULL
            LIMIT 1
            """;
        Optional<UserAccount> result = jdbcTemplate.query(sql, USER_ROW_MAPPER, userId).stream().findFirst();
        log.info(
            "SQL findProfileById userId={} -> found={} ({} ms)",
            userId,
            result.isPresent(),
            System.currentTimeMillis() - startTime
        );
        return result;
    }

    public void updateLoginSuccess(Long userId) {
        long startTime = System.currentTimeMillis();
        String sql = """
            UPDATE sys_users
            SET last_login_at = CURRENT_TIMESTAMP(3),
                login_count = login_count + 1
            WHERE id = ?
            """;
        jdbcTemplate.update(sql, userId);
        log.info(
            "SQL updateLoginSuccess userId={} ({} ms)",
            userId,
            System.currentTimeMillis() - startTime
        );
    }
}
