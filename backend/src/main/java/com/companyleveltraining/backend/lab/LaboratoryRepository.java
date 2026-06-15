package com.companyleveltraining.backend.lab;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.companyleveltraining.backend.lab.dto.LabResponse;
import com.companyleveltraining.backend.lab.dto.LabSaveRequest;

@Repository
public class LaboratoryRepository {

    private static final RowMapper<LabResponse> LAB_ROW_MAPPER = (rs, rowNum) -> {
        int capacity = rs.getInt("capacity");
        String status = rs.getString("status");
        return new LabResponse(
            rs.getLong("id"),
            rs.getString("lab_code"),
            rs.getString("name"),
            rs.getString("location"),
            rs.getString("equipment"),
            rs.getString("image_url"),
            capacity,
            capacity,
            status,
            statusText(status)
        );
    };

    private static String statusText(String status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case "active" -> "正常";
            case "maintenance" -> "维修中";
            case "disabled" -> "已停用";
            case "deleted" -> "已删除";
            default -> status;
        };
    }

    private final JdbcTemplate jdbcTemplate;

    public LaboratoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** 查询未删除的实验室列表，可按关键词模糊匹配名称/位置/设备。 */
    public List<LabResponse> findAll(String keyword, boolean onlyActive) {
        StringBuilder sql = new StringBuilder("""
            SELECT id, lab_code, name, location, equipment, image_url, capacity, status
            FROM laboratories
            WHERE deleted_at IS NULL
            """);
        java.util.List<Object> args = new java.util.ArrayList<>();
        if (onlyActive) {
            sql.append(" AND status = 'active' ");
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (name LIKE ? OR location LIKE ? OR equipment LIKE ?) ");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
            args.add(like);
        }
        sql.append(" ORDER BY id ASC ");
        return jdbcTemplate.query(sql.toString(), LAB_ROW_MAPPER, args.toArray());
    }

    public Optional<LabResponse> findById(Long id) {
        String sql = """
            SELECT id, lab_code, name, location, equipment, image_url, capacity, status
            FROM laboratories
            WHERE id = ? AND deleted_at IS NULL
            LIMIT 1
            """;
        return jdbcTemplate.query(sql, LAB_ROW_MAPPER, id).stream().findFirst();
    }

    public Long insert(LabSaveRequest req, String labCode, Long operatorId) {
        String sql = """
            INSERT INTO laboratories (lab_code, name, location, capacity, equipment, image_url, status, created_by, updated_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[] {"id"});
            ps.setString(1, labCode);
            ps.setString(2, req.name());
            ps.setString(3, req.location());
            ps.setInt(4, req.capacity() == null ? 0 : req.capacity());
            ps.setString(5, req.equipment());
            ps.setString(6, req.image());
            ps.setString(7, req.status() == null ? "active" : req.status());
            ps.setObject(8, operatorId);
            ps.setObject(9, operatorId);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public void update(Long id, LabSaveRequest req, Long operatorId) {
        String sql = """
            UPDATE laboratories
            SET name = ?, location = ?, capacity = ?, equipment = ?, image_url = ?,
                status = COALESCE(?, status), updated_by = ?
            WHERE id = ? AND deleted_at IS NULL
            """;
        jdbcTemplate.update(sql,
            req.name(),
            req.location(),
            req.capacity() == null ? 0 : req.capacity(),
            req.equipment(),
            req.image(),
            req.status(),
            operatorId,
            id
        );
    }

    public void softDelete(Long id, Long operatorId) {
        String sql = """
            UPDATE laboratories
            SET deleted_at = CURRENT_TIMESTAMP(3), status = 'deleted', updated_by = ?
            WHERE id = ? AND deleted_at IS NULL
            """;
        jdbcTemplate.update(sql, operatorId, id);
    }

    public boolean existsActiveReservation(Long labId) {
        String sql = """
            SELECT COUNT(*) FROM reservation_applications
            WHERE lab_id = ? AND deleted_at IS NULL
              AND status IN ('pending','teacher_approved','approved')
            """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, labId);
        return count != null && count > 0;
    }
}
