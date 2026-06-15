package com.companyleveltraining.backend.reservation;

import java.util.List;
import java.util.Map;

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
 * 教师审核接口，对应 teacher-pending-process / teacher-completed-process：
 * 待审核学生申请、已审记录、通过/驳回/重审。
 */
@RestController
@RequestMapping("/api/teacher/reservations")
public class TeacherReviewController {

    private final ReviewService reviewService;

    public TeacherReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/pending")
    public List<Map<String, Object>> pending() {
        SecurityUtils.requireRole("teacher");
        return reviewService.teacherPending();
    }

    @GetMapping("/pending-count")
    public Map<String, Object> pendingCount() {
        SecurityUtils.requireRole("teacher");
        return Map.of("count", reviewService.teacherPending().size());
    }

    @GetMapping("/reviewed")
    public List<Map<String, Object>> reviewed() {
        SecurityUtils.requireRole("teacher");
        return reviewService.teacherReviewed(SecurityUtils.currentUserId());
    }

    @PostMapping("/{id}/approve")
    public Map<String, Object> approve(@PathVariable Long id, @RequestBody(required = false) ReviewRequest req) {
        SecurityUtils.requireRole("teacher");
        SecurityUser teacher = SecurityUtils.currentUser();
        return reviewService.teacherApprove(id, teacher, req == null ? null : req.text());
    }

    @PostMapping("/{id}/reject")
    public Map<String, Object> reject(@PathVariable Long id, @RequestBody ReviewRequest req) {
        SecurityUtils.requireRole("teacher");
        return reviewService.teacherReject(id, SecurityUtils.currentUser(), req == null ? null : req.text());
    }

    @PostMapping("/{id}/reapprove")
    public Map<String, Object> reapprove(@PathVariable Long id, @RequestBody(required = false) ReviewRequest req) {
        SecurityUtils.requireRole("teacher");
        return reviewService.teacherReapprove(id, SecurityUtils.currentUser(), req == null ? null : req.text());
    }
}
