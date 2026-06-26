package com.companyleveltraining.backend.reservation;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.companyleveltraining.backend.common.SecurityUtils;
import com.companyleveltraining.backend.reservation.dto.ReviewRequest;
import com.companyleveltraining.backend.security.SecurityUser;

/**
 * 管理员预约审批接口，对应 admin-reservation-approval / admin-approval-records：
 * 全部预约列表、详情、终审通过/驳回、已审批记录与统计。
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "管理员预约审核", description = "管理员预约列表、详情、终审、审批记录和统计接口")
public class AdminReservationController {

    private final ReviewService reviewService;
    private final ReservationService reservationService;
    private final ReservationRepository repository;

    public AdminReservationController(ReviewService reviewService, ReservationService reservationService,
                                      ReservationRepository repository) {
        this.reviewService = reviewService;
        this.reservationService = reservationService;
        this.repository = repository;
    }

    @GetMapping("/reservations")
    @Operation(summary = "管理员预约列表", description = "管理员查询全部实验室预约申请")
    public List<Map<String, Object>> list() {
        SecurityUtils.requireRole("admin");
        return repository.findAllForAdmin();
    }

    @GetMapping("/reservations/{id}")
    @Operation(summary = "管理员预约详情", description = "管理员查询指定实验室预约申请详情")
    public Map<String, Object> detail(@PathVariable Long id) {
        SecurityUtils.requireRole("admin");
        return reservationService.getDetail(id);
    }

    @PostMapping("/reservations/{id}/approve")
    @Operation(summary = "管理员通过预约", description = "管理员终审通过指定预约申请")
    public Map<String, Object> approve(@PathVariable Long id, @RequestBody(required = false) ReviewRequest req) {
        SecurityUtils.requireRole("admin");
        SecurityUser admin = SecurityUtils.currentUser();
        return reviewService.adminApprove(id, admin, req == null ? null : req.text());
    }

    @PostMapping("/reservations/{id}/reject")
    @Operation(summary = "管理员驳回预约", description = "管理员终审驳回指定预约申请")
    public Map<String, Object> reject(@PathVariable Long id, @RequestBody ReviewRequest req) {
        SecurityUtils.requireRole("admin");
        return reviewService.adminReject(id, SecurityUtils.currentUser(), req == null ? null : req.text());
    }

    @GetMapping("/approval-records")
    @Operation(summary = "预约审批记录", description = "查询管理员预约审批记录")
    public List<Map<String, Object>> approvalRecords() {
        SecurityUtils.requireRole("admin");
        return repository.findApprovalRecords();
    }

    @GetMapping("/approval-records/stats")
    @Operation(summary = "预约审批统计", description = "统计预约审批记录中的通过、驳回和总数")
    public Map<String, Object> approvalStats() {
        SecurityUtils.requireRole("admin");
        List<Map<String, Object>> records = repository.findApprovalRecords();
        long approved = records.stream().filter(m -> "approved".equals(m.get("status"))).count();
        long rejected = records.stream().filter(m -> "rejected".equals(m.get("status"))).count();
        return Map.of(
            "total", records.size(),
            "approved", approved,
            "rejected", rejected
        );
    }
}
