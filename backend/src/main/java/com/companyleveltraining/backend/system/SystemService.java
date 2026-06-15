package com.companyleveltraining.backend.system;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.companyleveltraining.backend.common.BizNoGenerator;
import com.companyleveltraining.backend.common.BusinessException;

/**
 * 系统设置与数据备份业务，对应 admin-system-management 的「系统设置」「数据备份」两个 Tab。
 * 备份/恢复为模拟实现（生成记录并标记成功）。
 */
@Service
public class SystemService {

    private final JdbcTemplate jdbcTemplate;
    private final BizNoGenerator bizNoGenerator;

    public SystemService(JdbcTemplate jdbcTemplate, BizNoGenerator bizNoGenerator) {
        this.jdbcTemplate = jdbcTemplate;
        this.bizNoGenerator = bizNoGenerator;
    }

    // ---- 系统设置 ----

    public Map<String, Object> getSettings() {
        return jdbcTemplate.queryForList("""
            SELECT id, system_name AS systemName, school_name AS schoolName, logo_url AS logoUrl,
                   DATE_FORMAT(reservation_start_time, '%H:%i') AS reservationStartTime,
                   DATE_FORMAT(reservation_end_time, '%H:%i') AS reservationEndTime,
                   advance_days AS advanceDays,
                   auto_approval AS autoApproval,
                   reservation_notification_enabled AS reservationNotification,
                   approval_notification_enabled AS approvalNotification,
                   reminder_notification_enabled AS reminderNotification
            FROM system_settings ORDER BY id ASC LIMIT 1
            """).stream().findFirst()
            .orElseThrow(() -> BusinessException.notFound("尚未初始化系统设置"));
    }

    public Map<String, Object> updateSettings(Map<String, Object> body, Long operatorId) {
        Map<String, Object> current = getSettings();
        Long id = ((Number) current.get("id")).longValue();
        jdbcTemplate.update("""
            UPDATE system_settings SET
              system_name = COALESCE(?, system_name),
              school_name = COALESCE(?, school_name),
              logo_url = COALESCE(?, logo_url),
              reservation_start_time = COALESCE(?, reservation_start_time),
              reservation_end_time = COALESCE(?, reservation_end_time),
              advance_days = COALESCE(?, advance_days),
              auto_approval = COALESCE(?, auto_approval),
              reservation_notification_enabled = COALESCE(?, reservation_notification_enabled),
              approval_notification_enabled = COALESCE(?, approval_notification_enabled),
              reminder_notification_enabled = COALESCE(?, reminder_notification_enabled),
              updated_by = ?
            WHERE id = ?
            """,
            str(body.get("systemName")), str(body.get("schoolName")), str(body.get("logoUrl")),
            str(body.get("reservationStartTime")), str(body.get("reservationEndTime")),
            intOrNull(body.get("advanceDays")),
            boolInt(body.get("autoApproval")),
            boolInt(body.get("reservationNotification")),
            boolInt(body.get("approvalNotification")),
            boolInt(body.get("reminderNotification")),
            operatorId, id);
        return getSettings();
    }

    // ---- 数据备份 ----

    public List<Map<String, Object>> listBackups() {
        return jdbcTemplate.queryForList("""
            SELECT id, backup_no AS backupNo, backup_type AS type, backup_name AS name,
                   display_size AS size, status, progress,
                   DATE_FORMAT(started_at, '%Y-%m-%d %H:%i:%s') AS time,
                   DATE_FORMAT(completed_at, '%Y-%m-%d %H:%i:%s') AS completedTime,
                   DATE_FORMAT(restored_at, '%Y-%m-%d %H:%i:%s') AS restoredTime
            FROM data_backup_records
            WHERE deleted_at IS NULL
            ORDER BY started_at DESC
            """);
    }

    public Map<String, Object> createBackup(String type, Long operatorId) {
        String backupType = normalizeBackupType(type);
        String name = switch (backupType) {
            case "labs" -> "实验室数据备份";
            case "schedules" -> "课表数据备份";
            case "reservations" -> "预约数据备份";
            case "users" -> "用户数据备份";
            default -> "全部数据备份";
        };
        String backupNo = bizNoGenerator.generate("BKP");
        String displaySize = String.format("%.1fMB", 1.0 + Math.abs(backupNo.hashCode() % 50) / 10.0);
        jdbcTemplate.update("""
            INSERT INTO data_backup_records
              (backup_no, backup_type, backup_name, display_size, status, progress, created_by, started_at, completed_at)
            VALUES (?, ?, ?, ?, 'success', 100, ?, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3))
            """, backupNo, backupType, name, displaySize, operatorId);
        return getBackup(backupNo);
    }

    private Map<String, Object> getBackup(String backupNo) {
        return jdbcTemplate.queryForList("""
            SELECT id, backup_no AS backupNo, backup_type AS type, backup_name AS name,
                   display_size AS size, status, progress,
                   DATE_FORMAT(started_at, '%Y-%m-%d %H:%i:%s') AS time
            FROM data_backup_records WHERE backup_no = ? LIMIT 1
            """, backupNo).stream().findFirst()
            .orElseThrow(() -> BusinessException.notFound("备份记录不存在"));
    }

    public Map<String, Object> getBackupById(Long id) {
        return jdbcTemplate.queryForList("""
            SELECT id, backup_no AS backupNo, backup_type AS type, backup_name AS name,
                   display_size AS size, status, progress,
                   DATE_FORMAT(started_at, '%Y-%m-%d %H:%i:%s') AS time
            FROM data_backup_records WHERE id = ? AND deleted_at IS NULL LIMIT 1
            """, id).stream().findFirst()
            .orElseThrow(() -> BusinessException.notFound("备份记录不存在"));
    }

    public Map<String, Object> restoreBackup(Long id) {
        int updated = jdbcTemplate.update("""
            UPDATE data_backup_records SET restored_at = CURRENT_TIMESTAMP(3)
            WHERE id = ? AND deleted_at IS NULL AND status = 'success'
            """, id);
        if (updated == 0) {
            throw BusinessException.conflict("该备份不可恢复");
        }
        return Map.of("success", true, "message", "数据已从备份恢复");
    }

    public void deleteBackup(Long id) {
        jdbcTemplate.update("""
            UPDATE data_backup_records SET deleted_at = CURRENT_TIMESTAMP(3), status = 'deleted'
            WHERE id = ? AND deleted_at IS NULL
            """, id);
    }

    /** 兼容前端可能传中文/英文的备份类型，统一成 data_backup_records.backup_type 枚举值。 */
    private String normalizeBackupType(String type) {
        if (type == null || type.isBlank()) {
            return "all";
        }
        return switch (type.trim()) {
            case "labs", "实验室", "实验室数据" -> "labs";
            case "schedules", "课表", "课表数据" -> "schedules";
            case "reservations", "预约", "预约数据" -> "reservations";
            case "users", "用户", "用户数据" -> "users";
            case "all", "全部", "全部数据", "全量" -> "all";
            default -> "all";
        };
    }

    private String str(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    private Integer intOrNull(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(v));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Integer boolInt(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Boolean b) {
            return b ? 1 : 0;
        }
        String s = String.valueOf(v);
        if ("true".equalsIgnoreCase(s) || "1".equals(s)) {
            return 1;
        }
        if ("false".equalsIgnoreCase(s) || "0".equals(s)) {
            return 0;
        }
        return null;
    }
}
