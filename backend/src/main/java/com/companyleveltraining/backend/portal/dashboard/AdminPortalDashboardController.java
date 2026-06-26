package com.companyleveltraining.backend.portal.dashboard;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.companyleveltraining.backend.common.SecurityUtils;
import com.companyleveltraining.backend.common.cache.RedisCacheService;
import com.companyleveltraining.backend.portal.news.NewsService;
import com.companyleveltraining.backend.portal.notice.NoticeService;
import com.companyleveltraining.backend.portal.task.PortalTaskService;
import com.companyleveltraining.backend.reservation.ReservationRepository;
import com.companyleveltraining.backend.security.SecurityUser;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * 管理员综合工作台数据接口：数字概览 + 任务统计 + 通知中心 + 资讯中心 + 最近预约。
 * 与 /api/admin/dashboard/summary 互补：summary 偏待办计数，本接口偏工作台整屏聚合。
 */
@RestController
@RequestMapping("/api/admin/portal/dashboard")
public class AdminPortalDashboardController {

    private static final String DASHBOARD_SNAPSHOT_CACHE_KEY = "portal:admin:dashboard:snapshot";

    private final AdminDashboardMapper dashboardMapper;
    private final PortalTaskService portalTaskService;
    private final NoticeService noticeService;
    private final NewsService newsService;
    private final ReservationRepository reservationRepository;
    private final RedisCacheService redisCacheService;
    private final long dashboardCacheTtlSeconds;

    public AdminPortalDashboardController(AdminDashboardMapper dashboardMapper, PortalTaskService portalTaskService,
                                          NoticeService noticeService, NewsService newsService,
                                          ReservationRepository reservationRepository,
                                          RedisCacheService redisCacheService,
                                          @Value("${cache.redis.dashboard-ttl-seconds:30}") long dashboardCacheTtlSeconds) {
        this.dashboardMapper = dashboardMapper;
        this.portalTaskService = portalTaskService;
        this.noticeService = noticeService;
        this.newsService = newsService;
        this.reservationRepository = reservationRepository;
        this.redisCacheService = redisCacheService;
        this.dashboardCacheTtlSeconds = dashboardCacheTtlSeconds;
    }

    @GetMapping
    public Map<String, Object> dashboard() {
        SecurityUtils.requireRole("admin");
        SecurityUser admin = SecurityUtils.currentUser();

        Map<String, Object> snapshot = redisCacheService.getOrLoad(
            DASHBOARD_SNAPSHOT_CACHE_KEY,
            Duration.ofSeconds(dashboardCacheTtlSeconds),
            this::dashboardSnapshot,
            new TypeReference<Map<String, Object>>() {}
        );

        Map<String, Object> result = new LinkedHashMap<>(snapshot);
        result.put("taskStats", portalTaskService.stats(admin));
        result.put("recentNotices", limit(noticeService.adminList("published", null), 5));
        result.put("recentNews", limit(newsService.adminList("published", null, null), 5));
        result.put("recentReservations", limit(reservationRepository.findAllForAdmin(), 8));
        return result;
    }

    private Map<String, Object> dashboardSnapshot() {
        Map<String, Object> summary = new LinkedHashMap<>();
        long pendingReservations = dashboardMapper.countPendingReservations();
        long pendingTeacherReviews = dashboardMapper.countPendingTeacherReviews();
        long pendingTeacherRegistrations = dashboardMapper.countPendingTeacherRegistrations();
        long todayReservations = dashboardMapper.countTodayReservations();
        long weekReservations = dashboardMapper.countWeekReservations();
        long totalLabs = dashboardMapper.countTotalLabs();
        long activeLabs = dashboardMapper.countActiveLabs();
        long totalUsers = dashboardMapper.countTotalUsers();
        long totalApps = dashboardMapper.countTotalApps();
        long publishedNotices = dashboardMapper.countPublishedNotices();
        long approvedToday = dashboardMapper.countApprovedToday();
        int labUsageRate = totalLabs == 0 ? 0 : (int) Math.min(100, Math.round(approvedToday * 100.0 / totalLabs));

        summary.put("pendingReservations", pendingReservations);
        summary.put("pendingTeacherReviews", pendingTeacherReviews);
        summary.put("pendingTeacherRegistrations", pendingTeacherRegistrations);
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
        result.put("statusDistribution", statusDistribution());
        return result;
    }

    /** 预约状态分布（用于环形图）。 */
    private List<Map<String, Object>> statusDistribution() {
        return dashboardMapper.statusDistribution();
    }

    private <T> List<T> limit(List<T> list, int n) {
        return list.size() > n ? list.subList(0, n) : list;
    }
}
