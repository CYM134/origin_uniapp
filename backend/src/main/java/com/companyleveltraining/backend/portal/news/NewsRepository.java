package com.companyleveltraining.backend.portal.news;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.companyleveltraining.backend.portal.news.dto.NewsCategoryResponse;
import com.companyleveltraining.backend.portal.news.dto.NewsResponse;

/** 校园资讯数据访问。 */
@Repository
public class NewsRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NewsMapper newsMapper;

    public NewsRepository(JdbcTemplate jdbcTemplate, NewsMapper newsMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.newsMapper = newsMapper;
    }

    public List<NewsCategoryResponse> categories() {
        return newsMapper.categories().stream()
            .map(NewsRepository::mapCategory)
            .toList();
    }

    public List<NewsResponse> findPublished(Long categoryId, String keyword) {
        return newsMapper.findPublished(categoryId, normalize(keyword)).stream()
            .map(NewsRepository::mapNews)
            .toList();
    }

    public List<NewsResponse> findLatest(int limit) {
        return newsMapper.findLatest(limit).stream()
            .map(NewsRepository::mapNews)
            .toList();
    }

    public Optional<NewsResponse> findPublishedById(Long id) {
        return Optional.ofNullable(newsMapper.findPublishedById(id))
            .map(NewsRepository::mapNews);
    }

    public void incrementView(Long id) {
        jdbcTemplate.update("UPDATE news SET view_count = view_count + 1 WHERE id = ?", id);
    }

    // ---- 管理端 ----

    public List<NewsResponse> findAllForAdmin(String status, Long categoryId, String keyword) {
        return newsMapper.findAllForAdmin(normalize(status), categoryId, normalize(keyword)).stream()
            .map(NewsRepository::mapNews)
            .toList();
    }

    public Optional<NewsResponse> findByIdForAdmin(Long id) {
        return Optional.ofNullable(newsMapper.findByIdForAdmin(id))
            .map(NewsRepository::mapNews);
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
        return newsMapper.categoryCodeOf(categoryId);
    }

    private static NewsResponse mapNews(Map<String, Object> row) {
        return new NewsResponse(
            longValue(row, "id"),
            stringValue(row, "newsNo"),
            stringValue(row, "title"),
            stringValue(row, "summary"),
            stringValue(row, "content"),
            nullableLongValue(row, "categoryId"),
            stringValue(row, "categoryCode"),
            stringValue(row, "categoryName"),
            stringValue(row, "coverImage"),
            stringValue(row, "author"),
            stringValue(row, "status"),
            boolValue(row, "top"),
            stringValue(row, "publishTime"),
            nullableLongValue(row, "publisherId"),
            longValue(row, "viewCount"),
            stringValue(row, "createTime")
        );
    }

    private static NewsCategoryResponse mapCategory(Map<String, Object> row) {
        return new NewsCategoryResponse(
            longValue(row, "id"),
            stringValue(row, "categoryName"),
            stringValue(row, "categoryCode"),
            intValue(row, "sortOrder"),
            intValue(row, "status")
        );
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

    private static Integer intValue(Map<String, Object> row, String key) {
        Object value = value(row, key);
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean b) {
            return b ? 1 : 0;
        }
        return value instanceof Number n ? n.intValue() : Integer.valueOf(String.valueOf(value));
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
