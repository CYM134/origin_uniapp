package com.companyleveltraining.backend.ai;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final String[][] STANDARD_SLOTS = {
        {"08:00-10:00", "08:00:00", "10:00:00"},
        {"10:00-12:00", "10:00:00", "12:00:00"},
        {"14:00-16:00", "14:00:00", "16:00:00"},
        {"16:00-18:00", "16:00:00", "18:00:00"},
        {"19:00-21:00", "19:00:00", "21:00:00"}
    };

    private final AiContextMapper contextMapper;

    public AiContextService(AiContextMapper contextMapper) {
        this.contextMapper = contextMapper;
    }

    public AiContext build(SecurityUser user) {
        String userSummary = "当前用户：" + safe(user.realName(), user.accountNo()) + "，角色：" + roleText(user.role())
            + "，账号：" + safe(user.accountNo(), "-") + "。";
        String reservationSummary = reservationSummary(user);
        String taskSummary = taskSummary(user);
        String messageSummary = messageSummary(user);
        String calendarSummary = calendarSummary(user);
        String scheduleSummary = scheduleSummary(user);
        String availabilitySummary = availabilitySummary();
        String noticeSummary = noticeSummary(user);

        String prompt = String.join("\n",
            userSummary,
            reservationSummary,
            taskSummary,
            messageSummary,
            calendarSummary,
            scheduleSummary,
            availabilitySummary,
            noticeSummary
        );
        return new AiContext(prompt, reservationSummary, taskSummary, messageSummary, calendarSummary,
            scheduleSummary, availabilitySummary, noticeSummary);
    }

    private String reservationSummary(SecurityUser user) {
        try {
            if ("admin".equals(user.role())) {
                long pending = contextMapper.countAdminPendingReservations();
                List<Map<String, Object>> recent = contextMapper.findRecentReservationsForAdmin();
                return "预约摘要：当前等待管理员处理的预约 " + pending + " 条。最近预约："
                    + formatReservations(recent) + "。";
            }

            List<Map<String, Object>> mine = contextMapper.findRecentReservationsByApplicant(user.id());
            long active = contextMapper.countActiveReservationsByApplicant(user.id());
            return "预约摘要：我的进行中预约 " + active + " 条。最近我的预约：" + formatReservations(mine) + "。";
        } catch (Exception ex) {
            log.warn("生成预约上下文失败: {}", ex.getMessage());
            return "预约摘要：暂时无法读取预约数据。";
        }
    }

    private String taskSummary(SecurityUser user) {
        try {
            if ("admin".equals(user.role())) {
                long reservationTodos = contextMapper.countAdminPendingReservations();
                long teacherRegistrations = contextMapper.countPendingTeacherRegistrations();
                return "待办摘要：预约待终审 " + reservationTodos + " 条，教师注册待审核 "
                    + teacherRegistrations + " 条。";
            }
            if ("teacher".equals(user.role())) {
                long reviews = contextMapper.countStudentPendingTeacherReviews();
                return "待办摘要：学生预约待教师初审 " + reviews + " 条。";
            }
            long myPending = contextMapper.countPendingReservationsByApplicant(user.id());
            return "待办摘要：我的预约待审批 " + myPending + " 条。";
        } catch (Exception ex) {
            log.warn("生成待办上下文失败: {}", ex.getMessage());
            return "待办摘要：暂时无法读取待办数据。";
        }
    }

    private String messageSummary(SecurityUser user) {
        try {
            long unread = contextMapper.countUnreadMessages(user.id());
            List<Map<String, Object>> recent = contextMapper.findRecentMessages(user.id());
            return "消息摘要：未读消息 " + unread + " 条。最近消息：" + formatTitleList(recent, "createTime") + "。";
        } catch (Exception ex) {
            log.warn("生成消息上下文失败: {}", ex.getMessage());
            return "消息摘要：暂时无法读取消息数据。";
        }
    }

    private String calendarSummary(SecurityUser user) {
        try {
            List<String> items = new ArrayList<>();
            List<Map<String, Object>> events = contextMapper.findUpcomingCalendarEvents(user.id());
            for (Map<String, Object> ev : events) {
                items.add(summaryLine(ev.get("title"), ev.get("startTime"), ev.get("location")));
            }

            List<Map<String, Object>> reservations = contextMapper.findUpcomingReservationEvents(user.id());
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
            List<Map<String, Object>> notices = contextMapper.findVisibleNotices(user.role());
            return "公告摘要：最新可见公告：" + formatTitleList(notices, "publishTime") + "。";
        } catch (Exception ex) {
            log.warn("生成公告上下文失败: {}", ex.getMessage());
            return "公告摘要：暂时无法读取公告数据。";
        }
    }

    private String scheduleSummary(SecurityUser user) {
        try {
            List<Map<String, Object>> rows;
            if ("admin".equals(user.role())) {
                rows = contextMapper.findUpcomingSchedulesForAdmin();
                return "课表摘要：近期平台课表：" + formatSchedules(rows) + "。";
            }
            if ("teacher".equals(user.role())) {
                rows = contextMapper.findUpcomingSchedulesForTeacher(user.id());
                return "课表摘要：我近期的授课安排：" + formatSchedules(rows) + "。";
            }
            rows = contextMapper.findUpcomingSchedulesForStudent(user.id());
            return "课表摘要：我近期的课程安排：" + formatSchedules(rows) + "。";
        } catch (Exception ex) {
            log.warn("生成课表上下文失败: {}", ex.getMessage());
            return "课表摘要：暂时无法读取课表数据。";
        }
    }

    private String availabilitySummary() {
        try {
            List<Map<String, Object>> labs = contextMapper.findActiveLabsForAvailability();
            if (labs.isEmpty()) {
                return "实验室空余时间摘要：暂无可用实验室。";
            }
            LocalDate today = LocalDate.now();
            List<String> days = List.of(today.format(DATE_FORMATTER), today.plusDays(1).format(DATE_FORMATTER));
            List<String> lines = new ArrayList<>();
            for (Map<String, Object> lab : labs) {
                Long labId = toLongObj(lab.get("id"));
                if (labId == null) {
                    continue;
                }
                String labName = safeObj(lab.get("name"), "实验室");
                for (String day : days) {
                    List<String> freeSlots = new ArrayList<>();
                    for (String[] slot : STANDARD_SLOTS) {
                        long occupied = contextMapper.countLabOccupiedSlots(labId, day, slot[1], slot[2]);
                        if (occupied == 0) {
                            freeSlots.add(slot[0]);
                        }
                    }
                    lines.add(labName + " " + day + "空余：" + (freeSlots.isEmpty() ? "暂无" : String.join("、", freeSlots)));
                }
            }
            return "实验室空余时间摘要：" + (lines.isEmpty() ? "暂无可确认数据" : String.join("；", lines)) + "。";
        } catch (Exception ex) {
            log.warn("生成实验室空余时间上下文失败: {}", ex.getMessage());
            return "实验室空余时间摘要：暂时无法读取实验室空余时间数据。";
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

    private String formatSchedules(List<Map<String, Object>> rows) {
        if (rows.isEmpty()) {
            return "暂无";
        }
        List<String> items = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            items.add(safeObj(row.get("scheduleDate"), "-") + " " + safeObj(row.get("startTime"), "")
                + "-" + safeObj(row.get("endTime"), "") + " " + safeObj(row.get("courseName"), "课程")
                + " @" + safeObj(row.get("labName"), "实验室") + "，教师" + safeObj(row.get("teacherName"), "-")
                + "，" + scheduleStatusLabel(safeObj(row.get("status"), "")));
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

    private String scheduleStatusLabel(String status) {
        return switch (status) {
            case "available" -> "可预约";
            case "full" -> "已满员";
            case "ongoing" -> "进行中";
            case "cancelled" -> "已取消";
            case "scheduled" -> "仅供查看";
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

    private Long toLongObj(Object value) {
        if (value instanceof Number n) {
            return n.longValue();
        }
        if (value == null || String.valueOf(value).isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public record AiContext(
        String prompt,
        String reservationSummary,
        String taskSummary,
        String messageSummary,
        String calendarSummary,
        String scheduleSummary,
        String availabilitySummary,
        String noticeSummary
    ) {
    }
}
