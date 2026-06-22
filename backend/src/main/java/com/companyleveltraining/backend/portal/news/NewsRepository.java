package com.companyleveltraining.backend.portal.news;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.companyleveltraining.backend.common.JdbcUtils;
import com.companyleveltraining.backend.portal.news.dto.NewsCategoryResponse;
import com.companyleveltraining.backend.portal.news.dto.NewsResponse;

/** 校园资讯数据访问。 */
@Repository
public class NewsRepository {

    private static final RowMapper<NewsResponse> NEWS_MAPPER = (rs, i) -> new NewsResponse(
        rs.getLong("id"),
        rs.getString("news_no"),
        rs.getString("title"),
        rs.getString("summary"),
        rs.getString("content"),
        JdbcUtils.nullableLong(rs, "category_id"),
        rs.getString("category_code"),
        rs.getString("category_name"),
        rs.getString("cover_image"),
        rs.getString("author"),
        rs.getString("status"),
        rs.getInt("is_top") == 1,
        rs.getString("publishTime"),
        JdbcUtils.nullableLong(rs, "publisher_id"),
        rs.getLong("view_count"),
        rs.getString("createTime")
    );

    private static final RowMapper<NewsCategoryResponse> CATEGORY_MAPPER = (rs, i) -> new NewsCategoryResponse(
        rs.getLong("id"),
        rs.getString("category_name"),
        rs.getString("category_code"),
        rs.getInt("sort_order"),
        rs.getInt("status")
    );

    private static final String BASE_SELECT = """
        SELECT n.id, n.news_no, n.title, n.summary, n.content, n.category_id, n.category_code,
               c.category_name, n.cover_image, n.author, n.status, n.is_top, n.publisher_id, n.view_count,
               DATE_FORMAT(n.publish_time, '%Y-%m-%d %H:%i:%s') AS publishTime,
               DATE_FORMAT(n.created_at, '%Y-%m-%d %H:%i:%s') AS createTime
        FROM news n
        LEFT JOIN news_category c ON c.id = n.category_id
        """;

    private final JdbcTemplate jdbcTemplate;

    public NewsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<NewsCategoryResponse> categories() {
        return jdbcTemplate.query("""
            SELECT id, category_name, category_code, sort_order, status
            FROM news_category WHERE status = 1 ORDER BY sort_order ASC, id ASC
            """, CATEGORY_MAPPER);
    }

    public List<NewsResponse> findPublished(Long categoryId, String keyword) {
        StringBuilder sql = new StringBuilder(BASE_SELECT)
            .append(" WHERE n.status = 'published' AND n.deleted_at IS NULL ");
        List<Object> args = new ArrayList<>();
        if (categoryId != null) {
            sql.append(" AND n.category_id = ? ");
            args.add(categoryId);
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND n.title LIKE ? ");
            args.add("%" + keyword.trim() + "%");
        }
        sql.append(" ORDER BY n.is_top DESC, n.publish_time DESC, n.id DESC ");
        return jdbcTemplate.query(sql.toString(), NEWS_MAPPER, args.toArray());
    }

    public List<NewsResponse> findLatest(int limit) {
        String sql = BASE_SELECT + """
             WHERE n.status = 'published' AND n.deleted_at IS NULL
             ORDER BY n.is_top DESC, n.publish_time DESC, n.id DESC LIMIT ?
            """;
        return jdbcTemplate.query(sql, NEWS_MAPPER, limit);
    }

    public Optional<NewsResponse> findPublishedById(Long id) {
        String sql = BASE_SELECT + " WHERE n.id = ? AND n.status = 'published' AND n.deleted_at IS NULL LIMIT 1";
        return jdbcTemplate.query(sql, NEWS_MAPPER, id).stream().findFirst();
    }

    public void incrementView(Long id) {
        jdbcTemplate.update("UPDATE news SET view_count = view_count + 1 WHERE id = ?", id);
    }

    // ---- 管理端 ----

    public List<NewsResponse> findAllForAdmin(String status, Long categoryId, String keyword) {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append(" WHERE n.deleted_at IS NULL ");
        List<Object> args = new ArrayList<>();
        if (status != null && !status.isBlank()) {
            sql.append(" AND n.status = ? ");
            args.add(status);
        }
        if (categoryId != null) {
            sql.append(" AND n.category_id = ? ");
            args.add(categoryId);
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND n.title LIKE ? ");
            args.add("%" + keyword.trim() + "%");
        }
        sql.append(" ORDER BY n.is_top DESC, n.created_at DESC ");
        return jdbcTemplate.query(sql.toString(), NEWS_MAPPER, args.toArray());
    }

    public Optional<NewsResponse> findByIdForAdmin(Long id) {
        String sql = BASE_SELECT + " WHERE n.id = ? AND n.deleted_at IS NULL LIMIT 1";
        return jdbcTemplate.query(sql, NEWS_MAPPER, id).stream().findFirst();
    }

    public Long insert(String newsNo, String title, String summary, String content, Long categoryId,
                       String categoryCode, String coverImage, String author, boolean top,
                       String status, boolean publishNow, Long publisherId) {
        String sql = """
            INSERT INTO news
              (news_no, title, summary, content, category_id, category_code, cover_image, author,
               is_top, status, publish_time, publisher_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, %s, ?)
            """.formatted(publishNow ? "CURRENT_TIMESTAMP(3)" : "NULL");
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[] {"id"});
            ps.setString(1, newsNo);
            ps.setString(2, title);
            ps.setString(3, summary);
            ps.setString(4, content);
            ps.setObject(5, categoryId);
            ps.setString(6, categoryCode);
            ps.setString(7, coverImage);
            ps.setString(8, author);
            ps.setInt(9, top ? 1 : 0);
            ps.setString(10, status == null ? "draft" : status);
            ps.setObject(11, publisherId);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public void update(Long id, String title, String summary, String content, Long categoryId,
                       String categoryCode, String coverImage, String author, boolean top) {
        jdbcTemplate.update("""
            UPDATE news
            SET title = ?, summary = ?, content = ?, category_id = ?, category_code = ?,
                cover_image = ?, author = ?, is_top = ?
            WHERE id = ? AND deleted_at IS NULL
            """, title, summary, content, categoryId, categoryCode, coverImage, author, top ? 1 : 0, id);
    }

    public int updateStatus(Long id, String status) {
        if ("published".equals(status)) {
            return jdbcTemplate.update("""
                UPDATE news SET status = 'published',
                    publish_time = COALESCE(publish_time, CURRENT_TIMESTAMP(3))
                WHERE id = ? AND deleted_at IS NULL
                """, id);
        }
        return jdbcTemplate.update(
            "UPDATE news SET status = ? WHERE id = ? AND deleted_at IS NULL", status, id);
    }

    public int softDelete(Long id) {
        return jdbcTemplate.update(
            "UPDATE news SET deleted_at = CURRENT_TIMESTAMP(3) WHERE id = ? AND deleted_at IS NULL", id);
    }

    public String categoryCodeOf(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        List<String> codes = jdbcTemplate.query(
            "SELECT category_code FROM news_category WHERE id = ?",
            (rs, i) -> rs.getString(1), categoryId);
        return codes.isEmpty() ? null : codes.get(0);
    }
}
