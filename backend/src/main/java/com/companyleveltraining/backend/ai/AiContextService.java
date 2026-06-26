package com.companyleveltraining.backend.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.companyleveltraining.backend.security.SecurityUser;

/**
 * 为 AI 助手生成轻量业务上下文摘要。
 * 这里不做工具调用/RAG，只在回答前按当前用户角色查询少量关键数据，
 * 供模型或内置兜底回答“我的预约/待办/消息/日程/通知”等实际问题。
 */
@Service
public class AiContextService {

    private static final Logger log = LoggerFactory.getLogger(AiContextService.class);

    private final JdbcTemplate jdbcTemplate;

    public AiContextService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public AiContext build(SecurityUser user) {
        String userSummary = "当前用户：" + safe(user.realName(), user.accountNo()) + "，角色：" + roleText(user.role())
            + "，账号：" + safe(user.accountNo(), "-") + "。";
        String reservationSummary = reservationSummary(user);
        String taskSummary = taskSummary(user);
        String messageSummary = messageSummary(user);
        String calendarSummary = calendarSummary(user);
        String noticeSummary = noticeSummary(user);

        String prompt = String.join("\n",
            userSummary,
            reservationSummary,
            taskSummary,
            messageSummary,
            calendarSummary,
            noticeSummary
        );
        return new AiContext(prompt, reservationSummary, taskSummary, messageSummary, calendarSummary, noticeSummary);
    }

    private String reservationSummary(SecurityUser user) {
        try {
            if ("admin".equals(user.role())) {
                long pending = count("""
                    SELECT COUNT(*) FROM reservation_applications
                    WHERE deleted_at IS NULL AND (
                      (applicant_role = 'student' AND status = 'teacher_approved')
                      OR (applicant_role = 'teacher' AND status = 'pending'))
                    """);
                List<Map<String, Object>> recent = jdbcTemplate.queryForList("""
                    SELECT application_no AS applicationNo, applicant_name AS applicantName,
                           lab_name_snapshot AS labName, reserve_date AS reserveDate,
                           TIME_FORMAT(start_time, '%H:%i') AS startTime,
                           TIME_FORMAT(end_time, '%H:%i') AS endTime,
                           status
                    FROM reservation_applications
                    WHERE deleted_at IS NULL AND status <> 'draft'
                    ORDER BY submitted_at DESC, id DESC
                    LIMIT 5
                    """);
                return "预约摘要：当前等待管理员处理的预约 " + pending + " 条。最近预约："
                    + formatReservations(recent) + "。";
            }

            List<Map<String, Object>> mine = jdbcTemplate.queryForList("""
                SELECT application_no AS applicationNo, lab_name_snapshot AS labName, reserve_date AS reserveDate,
                       TIME_FORMAT(start_time, '%H:%i') AS startTime,
                       TIME_FORMAT(end_time, '%H:%i') AS endTime,
                       status
                FROM reservation_applications
                WHERE deleted_at IS NULL AND status <> 'draft' AND applicant_user_id = ?
                ORDER BY submitted_at DESC, id DESC
                LIMIT 5
                """, user.id());
            long active = count("""
                SELECT COUNT(*) FROM reservation_applications
                WHERE deleted_at IS NULL AND applicant_user_id = ?
                  AND status IN ('pending','teacher_approved','approved')
                """, user.id());
            return "预约摘要：我的进行中预约 " + active + " 条。最近我的预约：" + formatReservations(mine) + "。";
        } catch (Exception ex) {
            log.warn("生成预约上下文失败: {}", ex.getMessage());
            return "预约摘要：暂时无法读取预约数据。";
        }
    }

    private String taskSummary(SecurityUser user) {
        try {
            if ("admin".equals(user.role())) {
                long reservationTodos = count("""
                    SELECT COUNT(*) FROM reservation_applications
                    WHERE deleted_at IS NULL AND (
                      (applicant_role = 'student' AND status = 'teacher_approved')
                      OR (applicant_role = 'teacher' AND status = 'pending'))
                    """);
                long teacherRegistrations = count("""
                    SELECT COUNT(*) FROM teacher_registration_applications WHERE status = 'pending'
                    """);
                return "待办摘要：预约待终审 " + reservationTodos + " 条，教师注册待审核 "
                    + teacherRegistrations + " 条。";
            }
            if ("teacher".equals(user.role())) {
                long reviews = count("""
                    SELECT COUNT(*) FROM reservation_applications
                    WHERE deleted_at IS NULL AND applicant_role = 'student' AND status = 'pending'
                    """);
                return "待办摘要：学生预约待教师初审 " + reviews + " 条。";
            }
            long myPending = count("""
                SELECT COUNT(*) FROM reservation_applications
                WHERE deleted_at IS NULL AND applicant_user_id = ?
                  AND status IN ('pending','teacher_approved')
                """, user.id());
            return "待办摘要：我的预约待审批 " + myPending + " 条。";
        } catch (Exception ex) {
            log.warn("生成待办上下文失败: {}", ex.getMessage());
            return "待办摘要：暂时无法读取待办数据。";
        }
    }

    private String messageSummary(SecurityUser user) {
        try {
            long unread = count("SELECT COUNT(*) FROM notifications WHERE recipient_user_id = ? AND is_read = 0", user.id());
            List<Map<String, Object>> recent = jdbcTemplate.queryForList("""
                SELECT title, content, is_read AS isRead,
                       DATE_FORMAT(created_at, '%Y-%m-%d %H:%i') AS createTime
                FROM notifications
                WHERE recipient_user_id = ?
                ORDER BY created_at DESC
                LIMIT 3
                """, user.id());
            return "消息摘要：未读消息 " + unread + " 条。最近消息：" + formatTitleList(recent, "createTime") + "。";
        } catch (Exception ex) {
            log.warn("生成消息上下文失败: {}", ex.getMessage());
            return "消息摘要：暂时无法读取消息数据。";
        }
    }

    private String calendarSummary(SecurityUser user) {
        try {
            List<String> items = new ArrayList<>();
            List<Map<String, Object>> events = jdbcTemplate.queryForList("""
                SELECT title, location,
                       DATE_FORMAT(start_time, '%Y-%m-%d %H:%i') AS startTime,
                       DATE_FORMAT(end_time, '%Y-%m-%d %H:%i') AS endTime
                FROM calendar_event
                WHERE user_id = ? AND start_time >= CURRENT_TIMESTAMP(3)
                ORDER BY start_time ASC
                LIMIT 3
                """, user.id());
            for (Map<String, Object> ev : events) {
                items.add(summaryLine(ev.get("title"), ev.get("startTime"), ev.get("location")));
            }

            List<Map<String, Object>> reservations = jdbcTemplate.queryForList("""
                SELECT CONCAT(lab_name_snapshot, ' · ',
                       CASE status
                         WHEN 'pending' THEN '待审核'
                         WHEN 'teacher_approved' THEN '待管理员审核'
                         WHEN 'approved' THEN '已通过'
                         ELSE status END) AS title,
                       lab_name_snapshot AS location,
                       DATE_FORMAT(CONCAT(reserve_date, ' ', start_time), '%Y-%m-%d %H:%i') AS startTime
                FROM reservation_applications
                WHERE applicant_user_id = ? AND deleted_at IS NULL
                  AND status IN ('pending','teacher_approved','approved')
                  AND CONCAT(reserve_date, ' ', end_time) >= CURRENT_TIMESTAMP(3)
                ORDER BY reserve_date ASC, start_time ASC
                LIMIT 3
                """, user.id());
            for (Map<String, Object> r : reservations) {
                items.add(summaryLine(r.get("title"), r.get("startTime"), r.get("location")));
            }
            return "日程摘要：近期日程/预约：" + (items.isEmpty() ? "暂无" : String.join("；", items)) + "。";
        } catch (Exception ex) {
            log.warn("生成日程上下文失败: {}", ex.getMessage());
            return "日程摘要：暂时无法读取日程数据。";
        }
    }

    private String noticeSummary(SecurityUser user) {
        try {
            List<Map<String, Object>> notices = jdbcTemplate.queryForList("""
                SELECT title, publisher_name AS publisherName,
                       DATE_FORMAT(publish_time, '%Y-%m-%d %H:%i') AS publishTime
                FROM notice
                WHERE status = 'published' AND deleted_at IS NULL
                  AND (publish_scope = 'all' OR FIND_IN_SET(?, target_roles) > 0)
                ORDER BY is_top DESC, publish_time DESC, id DESC
                LIMIT 3
                """, user.role());
            return "公告摘要：最新可见公告：" + formatTitleList(notices, "publishTime") + "。";
        } catch (Exception ex) {
            log.warn("生成公告上下文失败: {}", ex.getMessage());
            return "公告摘要：暂时无法读取公告数据。";
        }
    }

    private String formatReservations(List<Map<String, Object>> rows) {
        if (rows.isEmpty()) {
            return "暂无";
        }
        List<String> items = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            String applicant = r.get("applicantName") == null ? "" : "，申请人" + r.get("applicantName");
            items.add(safeObj(r.get("applicationNo"), "-") + " " + safeObj(r.get("labName"), "实验室")
                + " " + safeObj(r.get("reserveDate"), "-") + " " + safeObj(r.get("startTime"), "")
                + "-" + safeObj(r.get("endTime"), "") + "，" + statusLabel(safeObj(r.get("status"), ""))
                + applicant);
        }
        return String.join("；", items);
    }

    private String formatTitleList(List<Map<String, Object>> rows, String timeKey) {
        if (rows.isEmpty()) {
            return "暂无";
        }
        List<String> items = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            items.add(summaryLine(row.get("title"), row.get(timeKey), row.get("publisherName")));
        }
        return String.join("；", items);
    }

    private String summaryLine(Object title, Object time, Object suffix) {
        String line = safeObj(title, "-");
        if (time != null && !String.valueOf(time).isBlank()) {
            line += "（" + time + "）";
        }
        if (suffix != null && !String.valueOf(suffix).isBlank()) {
            line += "@" + suffix;
        }
        return line;
    }

    private long count(String sql, Object... args) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class, args);
        return value == null ? 0L : value;
    }

    private String roleText(String role) {
        return switch (role) {
            case "admin" -> "管理员";
            case "teacher" -> "教师";
            case "student" -> "学生";
            default -> safe(role, "用户");
        };
    }

    private String statusLabel(String status) {
        return switch (status) {
            case "pending" -> "待审核";
            case "teacher_approved" -> "待管理员审核";
            case "approved" -> "已通过";
            case "rejected" -> "已拒绝";
            case "cancelled" -> "已取消";
            case "completed" -> "已完成";
            default -> status == null || status.isBlank() ? "未知状态" : status;
        };
    }

    private String safe(String primary, String fallback) {
        return primary == null || primary.isBlank() ? fallback : primary;
    }

    private String safeObj(Object value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String s = String.valueOf(value);
        return s.isBlank() ? fallback : s;
    }

    public record AiContext(
        String prompt,
        String reservationSummary,
        String taskSummary,
        String messageSummary,
        String calendarSummary,
        String noticeSummary
    ) {
    }
}
