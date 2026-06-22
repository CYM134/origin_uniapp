package com.companyleveltraining.backend.portal.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.companyleveltraining.backend.common.JdbcUtils;
import com.companyleveltraining.backend.portal.app.dto.AppCategoryResponse;
import com.companyleveltraining.backend.portal.app.dto.AppResponse;
import com.companyleveltraining.backend.portal.app.dto.AppSaveRequest;

/**
 * 应用中心数据访问。沿用项目 JdbcTemplate + RowMapper + GeneratedKeyHolder 写法。
 * 可见范围以 portal_app.visible_roles 逗号分隔角色编码（P0 简化方案），用 FIND_IN_SET 过滤。
 */
@Repository
public class PortalAppRepository {

    private static final RowMapper<AppResponse> APP_ROW_MAPPER = (rs, rowNum) -> new AppResponse(
        rs.getLong("id"),
        rs.getString("app_name"),
        rs.getString("app_code"),
        JdbcUtils.nullableLong(rs, "category_id"),
        rs.getString("category_name"),
        rs.getString("icon"),
        rs.getString("description"),
        rs.getString("route_path"),
        rs.getString("external_url"),
        rs.getString("open_type"),
        rs.getString("visible_roles"),
        rs.getInt("is_hot") == 1,
        rs.getInt("is_new") == 1,
        rs.getInt("favorite") == 1,
        rs.getInt("status"),
        rs.getInt("sort_order"),
        rs.getLong("visit_count")
    );

    private static final RowMapper<AppCategoryResponse> CATEGORY_ROW_MAPPER = (rs, rowNum) -> new AppCategoryResponse(
        rs.getLong("id"),
        rs.getString("category_name"),
        rs.getString("category_code"),
        rs.getString("icon"),
        rs.getInt("sort_order"),
        rs.getInt("status")
    );

    private static final String BASE_SELECT = """
        SELECT a.id, a.app_name, a.app_code, a.category_id, c.category_name, a.icon, a.description,
               a.route_path, a.external_url, a.open_type, a.visible_roles, a.is_hot, a.is_new,
               a.status, a.sort_order, a.visit_count,
               (f.id IS NOT NULL) AS favorite
        FROM portal_app a
        LEFT JOIN portal_app_category c ON c.id = a.category_id
        LEFT JOIN portal_user_app_favorite f ON f.app_id = a.id AND f.user_id = ?
        """;

    private final JdbcTemplate jdbcTemplate;

    public PortalAppRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AppCategoryResponse> findCategories() {
        return jdbcTemplate.query("""
            SELECT id, category_name, category_code, icon, sort_order, status
            FROM portal_app_category
            WHERE status = 1
            ORDER BY sort_order ASC, id ASC
            """, CATEGORY_ROW_MAPPER);
    }

    /** 当前用户按角色可见的已启用应用，可选按分类与关键词过滤。 */
    public List<AppResponse> findVisibleApps(String role, Long userId, Long categoryId, String keyword) {
        StringBuilder sql = new StringBuilder(BASE_SELECT)
            .append(" WHERE a.status = 1 AND FIND_IN_SET(?, a.visible_roles) > 0 ");
        List<Object> args = new ArrayList<>();
        args.add(userId);
        args.add(role);
        if (categoryId != null) {
            sql.append(" AND a.category_id = ? ");
            args.add(categoryId);
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (a.app_name LIKE ? OR a.app_code LIKE ?) ");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
        }
        sql.append(" ORDER BY a.sort_order ASC, a.id ASC ");
        return jdbcTemplate.query(sql.toString(), APP_ROW_MAPPER, args.toArray());
    }

    /** 当前用户收藏的、仍可见且启用的应用。 */
    public List<AppResponse> findFavorites(String role, Long userId) {
        String sql = BASE_SELECT + """
             WHERE a.status = 1 AND f.id IS NOT NULL AND FIND_IN_SET(?, a.visible_roles) > 0
             ORDER BY f.created_at DESC
            """;
        return jdbcTemplate.query(sql, APP_ROW_MAPPER, userId, role);
    }

    public Optional<AppResponse> findVisibleById(String role, Long userId, Long appId) {
        String sql = BASE_SELECT + " WHERE a.id = ? AND FIND_IN_SET(?, a.visible_roles) > 0 LIMIT 1";
        return jdbcTemplate.query(sql, APP_ROW_MAPPER, userId, appId, role).stream().findFirst();
    }

    public boolean existsApp(Long appId) {
        Integer n = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM portal_app WHERE id = ?", Integer.class, appId);
        return n != null && n > 0;
    }

    // ---- 收藏 ----

    public int addFavorite(Long userId, Long appId) {
        return jdbcTemplate.update(
            "INSERT IGNORE INTO portal_user_app_favorite (user_id, app_id) VALUES (?, ?)",
            userId, appId);
    }

    public int removeFavorite(Long userId, Long appId) {
        return jdbcTemplate.update(
            "DELETE FROM portal_user_app_favorite WHERE user_id = ? AND app_id = ?",
            userId, appId);
    }

    // ---- 访问统计 ----

    public void recordVisit(Long userId, Long appId, String clientType, String ip) {
        jdbcTemplate.update("""
            INSERT INTO portal_app_visit_log (user_id, app_id, client_type, ip_address)
            VALUES (?, ?, ?, ?)
            """, userId, appId, clientType, ip);
        jdbcTemplate.update("UPDATE portal_app SET visit_count = visit_count + 1 WHERE id = ?", appId);
    }

    public Long currentVisitCount(Long appId) {
        return jdbcTemplate.queryForObject("SELECT visit_count FROM portal_app WHERE id = ?", Long.class, appId);
    }

    // ---- 管理端 ----

    /** 管理端：全部应用（含停用），可选关键词/分类过滤；favorite 恒为 0。 */
    public List<AppResponse> findAllForAdmin(Long categoryId, String keyword) {
        StringBuilder sql = new StringBuilder("""
            SELECT a.id, a.app_name, a.app_code, a.category_id, c.category_name, a.icon, a.description,
                   a.route_path, a.external_url, a.open_type, a.visible_roles, a.is_hot, a.is_new,
                   a.status, a.sort_order, a.visit_count, 0 AS favorite
            FROM portal_app a
            LEFT JOIN portal_app_category c ON c.id = a.category_id
            WHERE 1 = 1
            """);
        List<Object> args = new ArrayList<>();
        if (categoryId != null) {
            sql.append(" AND a.category_id = ? ");
            args.add(categoryId);
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (a.app_name LIKE ? OR a.app_code LIKE ?) ");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
        }
        sql.append(" ORDER BY a.sort_order ASC, a.id ASC ");
        return jdbcTemplate.query(sql.toString(), APP_ROW_MAPPER, args.toArray());
    }

    public Optional<AppResponse> findByIdForAdmin(Long id) {
        String sql = """
            SELECT a.id, a.app_name, a.app_code, a.category_id, c.category_name, a.icon, a.description,
                   a.route_path, a.external_url, a.open_type, a.visible_roles, a.is_hot, a.is_new,
                   a.status, a.sort_order, a.visit_count, 0 AS favorite
            FROM portal_app a
            LEFT JOIN portal_app_category c ON c.id = a.category_id
            WHERE a.id = ? LIMIT 1
            """;
        return jdbcTemplate.query(sql, APP_ROW_MAPPER, id).stream().findFirst();
    }

    public boolean existsAppCode(String appCode, Long excludeId) {
        Integer n = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM portal_app WHERE app_code = ? AND id <> ?",
            Integer.class, appCode, excludeId == null ? -1L : excludeId);
        return n != null && n > 0;
    }

    public Long insert(AppSaveRequest req) {
        String sql = """
            INSERT INTO portal_app
              (app_name, app_code, category_id, icon, description, route_path, external_url,
               open_type, visible_roles, is_hot, is_new, status, sort_order)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[] {"id"});
            ps.setString(1, req.appName());
            ps.setString(2, req.appCode());
            ps.setObject(3, req.categoryId());
            ps.setString(4, req.icon());
            ps.setString(5, req.description());
            ps.setString(6, req.routePath());
            ps.setString(7, req.externalUrl());
            ps.setString(8, req.openType() == null ? "internal" : req.openType());
            ps.setString(9, req.visibleRoles() == null || req.visibleRoles().isBlank()
                ? "admin,teacher,student" : req.visibleRoles());
            ps.setInt(10, Boolean.TRUE.equals(req.hot()) ? 1 : 0);
            ps.setInt(11, Boolean.TRUE.equals(req.latest()) ? 1 : 0);
            ps.setInt(12, req.status() == null ? 1 : req.status());
            ps.setInt(13, req.sortOrder() == null ? 0 : req.sortOrder());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public void update(Long id, AppSaveRequest req) {
        jdbcTemplate.update("""
            UPDATE portal_app
            SET app_name = ?, app_code = ?, category_id = ?, icon = ?, description = ?,
                route_path = ?, external_url = ?, open_type = COALESCE(?, open_type),
                visible_roles = COALESCE(?, visible_roles), is_hot = ?, is_new = ?,
                status = COALESCE(?, status), sort_order = COALESCE(?, sort_order)
            WHERE id = ?
            """,
            req.appName(), req.appCode(), req.categoryId(), req.icon(), req.description(),
            req.routePath(), req.externalUrl(), req.openType(),
            req.visibleRoles(),
            Boolean.TRUE.equals(req.hot()) ? 1 : 0,
            Boolean.TRUE.equals(req.latest()) ? 1 : 0,
            req.status(), req.sortOrder(), id);
    }

    public int updateStatus(Long id, Integer status) {
        return jdbcTemplate.update("UPDATE portal_app SET status = ? WHERE id = ?", status, id);
    }

    public int updateSort(Long id, Integer sortOrder) {
        return jdbcTemplate.update("UPDATE portal_app SET sort_order = ? WHERE id = ?", sortOrder, id);
    }

    public int delete(Long id) {
        jdbcTemplate.update("DELETE FROM portal_user_app_favorite WHERE app_id = ?", id);
        return jdbcTemplate.update("DELETE FROM portal_app WHERE id = ?", id);
    }
}
