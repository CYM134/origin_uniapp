package com.companyleveltraining.backend.portal.notice;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.companyleveltraining.backend.common.JdbcUtils;
import com.companyleveltraining.backend.portal.notice.dto.NoticeResponse;

/** 通知公告数据访问。日期统一用 DATE_FORMAT 返回字符串，对齐项目其它接口风格。 */
@Repository
public class NoticeRepository {

    private static NoticeResponse map(java.sql.ResultSet rs, boolean withRead) throws java.sql.SQLException {
        return new NoticeResponse(
            rs.getLong("id"),
            rs.getString("notice_no"),
            rs.getString("title"),
            rs.getString("content"),
            rs.getString("notice_type"),
            rs.getString("publish_scope"),
            rs.getString("target_roles"),
            rs.getInt("is_top") == 1,
            rs.getString("status"),
            rs.getString("publishTime"),
            JdbcUtils.nullableLong(rs, "publisher_id"),
            rs.getString("publisher_name"),
            rs.getLong("view_count"),
            rs.getString("createTime"),
            withRead && rs.getInt("read_flag") == 1
        );
    }

    private static final RowMapper<NoticeResponse> PORTAL_MAPPER = (rs, i) -> map(rs, true);
    private static final RowMapper<NoticeResponse> ADMIN_MAPPER = (rs, i) -> map(rs, false);

    private static final String PORTAL_SELECT = """
        SELECT n.id, n.notice_no, n.title, n.content, n.notice_type, n.publish_scope, n.target_roles,
               n.is_top, n.status, n.publisher_id, n.publisher_name, n.view_count,
               DATE_FORMAT(n.publish_time, '%Y-%m-%d %H:%i:%s') AS publishTime,
               DATE_FORMAT(n.created_at, '%Y-%m-%d %H:%i:%s') AS createTime,
               (r.id IS NOT NULL) AS read_flag
        FROM notice n
        LEFT JOIN notice_read r ON r.notice_id = n.id AND r.user_id = ?
        """;

    private final JdbcTemplate jdbcTemplate;

    public NoticeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** 门户：当前角色可见的已发布公告。 */
    public List<NoticeResponse> findPublishedForRole(String role, Long userId, String keyword) {
        StringBuilder sql = new StringBuilder(PORTAL_SELECT).append("""
             WHERE n.status = 'published' AND n.deleted_at IS NULL
               AND (n.publish_scope = 'all' OR FIND_IN_SET(?, n.target_roles) > 0)
            """);
        List<Object> args = new ArrayList<>();
        args.add(userId);
        args.add(role);
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND n.title LIKE ? ");
            args.add("%" + keyword.trim() + "%");
        }
        sql.append(" ORDER BY n.is_top DESC, n.publish_time DESC, n.id DESC ");
        return jdbcTemplate.query(sql.toString(), PORTAL_MAPPER, args.toArray());
    }

    public List<NoticeResponse> findLatestForRole(String role, Long userId, int limit) {
        String sql = PORTAL_SELECT + """
             WHERE n.status = 'published' AND n.deleted_at IS NULL
               AND (n.publish_scope = 'all' OR FIND_IN_SET(?, n.target_roles) > 0)
             ORDER BY n.is_top DESC, n.publish_time DESC, n.id DESC
             LIMIT ?
            """;
        return jdbcTemplate.query(sql, PORTAL_MAPPER, userId, role, limit);
    }

    public Optional<NoticeResponse> findPublishedById(String role, Long userId, Long id) {
        String sql = PORTAL_SELECT + """
             WHERE n.id = ? AND n.status = 'published' AND n.deleted_at IS NULL
               AND (n.publish_scope = 'all' OR FIND_IN_SET(?, n.target_roles) > 0)
             LIMIT 1
            """;
        return jdbcTemplate.query(sql, PORTAL_MAPPER, userId, id, role).stream().findFirst();
    }

    public void markRead(Long noticeId, Long userId) {
        int inserted = jdbcTemplate.update(
            "INSERT IGNORE INTO notice_read (notice_id, user_id) VALUES (?, ?)", noticeId, userId);
        if (inserted > 0) {
            jdbcTemplate.update("UPDATE notice SET view_count = view_count + 1 WHERE id = ?", noticeId);
        }
    }

    // ---- 管理端 ----

    private static final String ADMIN_SELECT = """
        SELECT n.id, n.notice_no, n.title, n.content, n.notice_type, n.publish_scope, n.target_roles,
               n.is_top, n.status, n.publisher_id, n.publisher_name, n.view_count,
               DATE_FORMAT(n.publish_time, '%Y-%m-%d %H:%i:%s') AS publishTime,
               DATE_FORMAT(n.created_at, '%Y-%m-%d %H:%i:%s') AS createTime
        FROM notice n
        """;

    public List<NoticeResponse> findAllForAdmin(String status, String keyword) {
        StringBuilder sql = new StringBuilder(ADMIN_SELECT).append(" WHERE n.deleted_at IS NULL ");
        List<Object> args = new ArrayList<>();
        if (status != null && !status.isBlank()) {
            sql.append(" AND n.status = ? ");
            args.add(status);
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND n.title LIKE ? ");
            args.add("%" + keyword.trim() + "%");
        }
        sql.append(" ORDER BY n.is_top DESC, n.created_at DESC ");
        return jdbcTemplate.query(sql.toString(), ADMIN_MAPPER, args.toArray());
    }

    public Optional<NoticeResponse> findByIdForAdmin(Long id) {
        String sql = ADMIN_SELECT + " WHERE n.id = ? AND n.deleted_at IS NULL LIMIT 1";
        return jdbcTemplate.query(sql, ADMIN_MAPPER, id).stream().findFirst();
    }

    public Long insert(String noticeNo, String title, String content, String noticeType,
                       String publishScope, String targetRoles, boolean top, String status,
                       boolean publishNow, Long publisherId, String publisherName) {
        String sql = """
            INSERT INTO notice
              (notice_no, title, content, notice_type, publish_scope, target_roles, is_top, status,
               publish_time, publisher_id, publisher_name)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, %s, ?, ?)
            """.formatted(publishNow ? "CURRENT_TIMESTAMP(3)" : "NULL");
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[] {"id"});
            ps.setString(1, noticeNo);
            ps.setString(2, title);
            ps.setString(3, content);
            ps.setString(4, noticeType == null ? "system" : noticeType);
            ps.setString(5, publishScope == null ? "all" : publishScope);
            ps.setString(6, targetRoles);
            ps.setInt(7, top ? 1 : 0);
            ps.setString(8, status == null ? "draft" : status);
            ps.setObject(9, publisherId);
            ps.setString(10, publisherName);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public void update(Long id, String title, String content, String noticeType,
                       String publishScope, String targetRoles, boolean top) {
        jdbcTemplate.update("""
            UPDATE notice
            SET title = ?, content = ?, notice_type = ?, publish_scope = ?, target_roles = ?, is_top = ?
            WHERE id = ? AND deleted_at IS NULL
            """, title, content, noticeType, publishScope, targetRoles, top ? 1 : 0, id);
    }

    /** 切换状态：published 时若未发布过则补 publish_time。 */
    public int updateStatus(Long id, String status) {
        if ("published".equals(status)) {
            return jdbcTemplate.update("""
                UPDATE notice
                SET status = 'published',
                    publish_time = COALESCE(publish_time, CURRENT_TIMESTAMP(3))
                WHERE id = ? AND deleted_at IS NULL
                """, id);
        }
        return jdbcTemplate.update(
            "UPDATE notice SET status = ? WHERE id = ? AND deleted_at IS NULL", status, id);
    }

    public int softDelete(Long id) {
        return jdbcTemplate.update(
            "UPDATE notice SET deleted_at = CURRENT_TIMESTAMP(3) WHERE id = ? AND deleted_at IS NULL", id);
    }
}
