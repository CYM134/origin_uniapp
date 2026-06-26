package com.companyleveltraining.backend.dashboard;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.companyleveltraining.backend.common.SecurityUtils;
import com.companyleveltraining.backend.portal.dashboard.AdminDashboardMapper;

/**
 * 管理员工作台汇总接口，对应 admin-work 的待办计数（可选增强）。
 */
@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardMapper dashboardMapper;

    public AdminDashboardController(AdminDashboardMapper dashboardMapper) {
        this.dashboardMapper = dashboardMapper;
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        SecurityUtils.requireRole("admin");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pendingReservations", dashboardMapper.countPendingReservations());
        result.put("pendingTeacherReviews", dashboardMapper.countPendingTeacherReviews());
        result.put("pendingTeacherRegistrations", dashboardMapper.countPendingTeacherRegistrations());
        result.put("totalLabs", dashboardMapper.countTotalLabs());
        result.put("totalUsers", dashboardMapper.countTotalUsers());
        return result;
    }
}
