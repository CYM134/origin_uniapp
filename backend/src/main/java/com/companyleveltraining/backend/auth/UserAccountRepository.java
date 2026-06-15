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

    /**
     * 轻量查询：仅取出账号状态与软删时间，用于过滤器每请求回库校验账号有效性。
     * 返回空表示用户不存在。
     */
    public Optional<AccountStatus> findAccountStatusById(long userId) {
        String sql = """
            SELECT status, deleted_at
            FROM sys_users
            WHERE id = ?
            LIMIT 1
            """;
        return jdbcTemplate.query(
            sql,
            (rs, rowNum) -> new AccountStatus(
                rs.getString("status"),
                rs.getTimestamp("deleted_at") != null
            ),
            userId
        ).stream().findFirst();
    }

    /**
     * 账号状态快照：status 取值（active/disabled/deleted 等）与是否已软删（deleted_at 非空）。
     */
    public record AccountStatus(String status, boolean deleted) {
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
