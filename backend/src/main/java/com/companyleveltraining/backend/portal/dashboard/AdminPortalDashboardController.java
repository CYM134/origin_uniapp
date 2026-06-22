package com.companyleveltraining.backend.portal.dashboard;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.companyleveltraining.backend.common.SecurityUtils;
import com.companyleveltraining.backend.portal.news.NewsService;
import com.companyleveltraining.backend.portal.notice.NoticeService;
import com.companyleveltraining.backend.portal.task.PortalTaskService;
import com.companyleveltraining.backend.reservation.ReservationRepository;
import com.companyleveltraining.backend.security.SecurityUser;

/**
 * 管理员综合工作台数据接口：数字概览 + 任务统计 + 通知中心 + 资讯中心 + 最近预约。
 * 与 /api/admin/dashboard/summary 互补：summary 偏待办计数，本接口偏工作台整屏聚合。
 */
@RestController
@RequestMapping("/api/admin/portal/dashboard")
public class AdminPortalDashboardController {

    private final JdbcTemplate jdbcTemplate;
    private final PortalTaskService portalTaskService;
    private final NoticeService noticeService;
    private final NewsService newsService;
    private final ReservationRepository reservationRepository;

    public AdminPortalDashboardController(JdbcTemplate jdbcTemplate, PortalTaskService portalTaskService,
                                          NoticeService noticeService, NewsService newsService,
                                          ReservationRepository reservationRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.portalTaskService = portalTaskService;
        this.noticeService = noticeService;
        this.newsService = newsService;
        this.reservationRepository = reservationRepository;
    }

    @GetMapping
    public Map<String, Object> dashboard() {
        SecurityUtils.requireRole("admin");
        SecurityUser admin = SecurityUtils.currentUser();

        Map<String, Object> summary = new LinkedHashMap<>();
        long pendingReservations = count("""
            SELECT COUNT(*) FROM reservation_applications
            WHERE deleted_at IS NULL AND (
              (applicant_role = 'student' AND status = 'teacher_approved')
              OR (applicant_role = 'teacher' AND status = 'pending'))
            """);
        long pendingTeacherReviews = count("""
            SELECT COUNT(*) FROM reservation_applications
            WHERE deleted_at IS NULL AND applicant_role = 'student' AND status = 'pending'
            """);
        long todayReservations = count("""
            SELECT COUNT(*) FROM reservation_applications
            WHERE deleted_at IS NULL AND reserve_date = CURDATE()
              AND status IN ('pending','teacher_approved','approved','completed')
            """);
        long weekReservations = count("""
            SELECT COUNT(*) FROM reservation_applications
            WHERE deleted_at IS NULL AND YEARWEEK(reserve_date, 1) = YEARWEEK(CURDATE(), 1)
              AND status IN ('pending','teacher_approved','approved','completed')
            """);
        long totalLabs = count("SELECT COUNT(*) FROM laboratories WHERE deleted_at IS NULL");
        long activeLabs = count("SELECT COUNT(*) FROM laboratories WHERE deleted_at IS NULL AND status = 'active'");
        long totalUsers = count("SELECT COUNT(*) FROM sys_users WHERE deleted_at IS NULL AND status <> 'deleted'");
        long totalApps = count("SELECT COUNT(*) FROM portal_app WHERE status = 1");
        long publishedNotices = count("SELECT COUNT(*) FROM notice WHERE status = 'published' AND deleted_at IS NULL");
        long approvedToday = count("""
            SELECT COUNT(*) FROM reservation_applications
            WHERE deleted_at IS NULL AND reserve_date = CURDATE() AND status IN ('approved','completed')
            """);
        int labUsageRate = totalLabs == 0 ? 0 : (int) Math.min(100, Math.round(approvedToday * 100.0 / totalLabs));

        summary.put("pendingReservations", pendingReservations);
        summary.put("pendingTeacherReviews", pendingTeacherReviews);
        summary.put("todayReservations", todayReservations);
        summary.put("weekReservations", weekReservations);
        summary.put("totalLabs", totalLabs);
        summary.put("activeLabs", activeLabs);
        summary.put("totalUsers", totalUsers);
        summary.put("totalApps", totalApps);
        summary.put("publishedNotices", publishedNotices);
        summary.put("labUsageRate", labUsageRate);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("summary", summary);
        result.put("taskStats", portalTaskService.stats(admin));
        result.put("statusDistribution", statusDistribution());
        result.put("recentNotices", limit(noticeService.adminList("published", null), 5));
        result.put("recentNews", limit(newsService.adminList("published", null, null), 5));
        result.put("recentReservations", limit(reservationRepository.findAllForAdmin(), 8));
        return result;
    }

    /** 预约状态分布（用于环形图）。 */
    private List<Map<String, Object>> statusDistribution() {
        return jdbcTemplate.query("""
            SELECT status, COUNT(*) AS cnt
            FROM reservation_applications
            WHERE deleted_at IS NULL AND status <> 'draft'
            GROUP BY status
            """, (rs, i) -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("status", rs.getString("status"));
                m.put("count", rs.getLong("cnt"));
                return m;
            });
    }

    private <T> List<T> limit(List<T> list, int n) {
        return list.size() > n ? list.subList(0, n) : list;
    }

    private long count(String sql) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class);
        return value == null ? 0L : value;
    }
}
