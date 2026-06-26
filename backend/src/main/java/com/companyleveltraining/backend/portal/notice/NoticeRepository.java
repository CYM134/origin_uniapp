package com.companyleveltraining.backend.portal.notice;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.companyleveltraining.backend.portal.notice.dto.NoticeResponse;

/** 通知公告数据访问。日期统一用 DATE_FORMAT 返回字符串，对齐项目其它接口风格。 */
@Repository
public class NoticeRepository {

    private static NoticeResponse map(Map<String, Object> row) {
        return new NoticeResponse(
            longValue(row, "id"),
            stringValue(row, "noticeNo"),
            stringValue(row, "title"),
            stringValue(row, "content"),
            stringValue(row, "noticeType"),
            stringValue(row, "publishScope"),
            stringValue(row, "targetRoles"),
            boolValue(row, "top"),
            stringValue(row, "status"),
            stringValue(row, "publishTime"),
            nullableLongValue(row, "publisherId"),
            stringValue(row, "publisherName"),
            longValue(row, "viewCount"),
            stringValue(row, "createTime"),
            boolValue(row, "readFlag")
        );
    }

    private final JdbcTemplate jdbcTemplate;
    private final NoticeMapper noticeMapper;

    public NoticeRepository(JdbcTemplate jdbcTemplate, NoticeMapper noticeMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.noticeMapper = noticeMapper;
    }

    /** 门户：当前角色可见的已发布公告。 */
    public List<NoticeResponse> findPublishedForRole(String role, Long userId, String keyword) {
        return noticeMapper.findPublishedForRole(role, userId, normalize(keyword)).stream()
            .map(NoticeRepository::map)
            .toList();
    }

    public List<NoticeResponse> findLatestForRole(String role, Long userId, int limit) {
        return noticeMapper.findLatestForRole(role, userId, limit).stream()
            .map(NoticeRepository::map)
            .toList();
    }

    public Optional<NoticeResponse> findPublishedById(String role, Long userId, Long id) {
        return Optional.ofNullable(noticeMapper.findPublishedById(role, userId, id))
            .map(NoticeRepository::map);
    }

    public void markRead(Long noticeId, Long userId) {
        int inserted = jdbcTemplate.update(
            "INSERT IGNORE INTO notice_read (notice_id, user_id) VALUES (?, ?)", noticeId, userId);
        if (inserted > 0) {
            jdbcTemplate.update("UPDATE notice SET view_count = view_count + 1 WHERE id = ?", noticeId);
        }
    }

    // ---- 管理端 ----

    public List<NoticeResponse> findAllForAdmin(String status, String keyword) {
        return noticeMapper.findAllForAdmin(normalize(status), normalize(keyword)).stream()
            .map(NoticeRepository::map)
            .toList();
    }

    public Optional<NoticeResponse> findByIdForAdmin(Long id) {
        return Optional.ofNullable(noticeMapper.findByIdForAdmin(id))
            .map(NoticeRepository::map);
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

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static Object value(Map<String, Object> row, String key) {
        if (row.containsKey(key)) {
            return row.get(key);
        }
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static String stringValue(Map<String, Object> row, String key) {
        Object value = value(row, key);
        return value == null ? null : String.valueOf(value);
    }

    private static Long nullableLongValue(Map<String, Object> row, String key) {
        Object value = value(row, key);
        if (value == null) {
            return null;
        }
        return value instanceof Number n ? n.longValue() : Long.valueOf(String.valueOf(value));
    }

    private static Long longValue(Map<String, Object> row, String key) {
        Long value = nullableLongValue(row, key);
        return value == null ? 0L : value;
    }

    private static boolean boolValue(Map<String, Object> row, String key) {
        Object value = value(row, key);
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof Number n) {
            return n.intValue() != 0;
        }
        return value != null && Boolean.parseBoolean(String.valueOf(value));
    }
}
