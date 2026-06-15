package com.companyleveltraining.backend.reservation;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.companyleveltraining.backend.common.BusinessException;
import com.companyleveltraining.backend.common.SecurityUtils;
import com.companyleveltraining.backend.reservation.dto.StudentReservationRequest;
import com.companyleveltraining.backend.reservation.dto.TeacherReservationRequest;
import com.companyleveltraining.backend.security.SecurityUser;

/**
 * 预约申请接口：学生/教师发起预约、查看本人申请、详情、取消、完成、统计。
 */
@RestController
@RequestMapping("/api")
public class ReservationController {

    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    @PostMapping("/student/reservations")
    public Map<String, Object> createStudent(@Valid @RequestBody StudentReservationRequest req) {
        SecurityUtils.requireRole("student");
        return service.createStudentReservation(SecurityUtils.currentUser(), req);
    }

    @PostMapping("/teacher/reservations")
    public Map<String, Object> createTeacher(@Valid @RequestBody TeacherReservationRequest req) {
        SecurityUtils.requireRole("teacher");
        return service.createTeacherReservation(SecurityUtils.currentUser(), req);
    }

    @GetMapping("/reservations/mine")
    public List<Map<String, Object>> mine() {
        return service.listMine(SecurityUtils.currentUserId());
    }

    @GetMapping("/reservations/mine/stats")
    public Map<String, Object> myStats() {
        return service.getMyStats(SecurityUtils.currentUserId());
    }

    @GetMapping("/reservations/{id}")
    public Map<String, Object> detail(@PathVariable Long id) {
        SecurityUser user = SecurityUtils.currentUser();
        Map<String, Object> detail = service.getDetail(id);
        if (!canView(user, detail)) {
            throw BusinessException.forbidden("无权查看他人的预约申请");
        }
        return detail;
    }

    /**
     * 详情授权（#12）：admin 可看全部；teacher 可看学生申请（审核共享池）或本人相关记录
     * （本人申请 / 本人为教师审核人）；student 仅本人；其余拒绝。
     */
    private boolean canView(SecurityUser user, Map<String, Object> detail) {
        if ("admin".equals(user.role())) {
            return true;
        }
        boolean isOwner = idEquals(user.id(), detail.get("applicantUserId"));
        if ("teacher".equals(user.role())) {
            return "student".equals(detail.get("applicantRole"))
                || isOwner
                || idEquals(user.id(), detail.get("teacherReviewerId"));
        }
        // student：仅本人
        return isOwner;
    }

    private boolean idEquals(Long userId, Object value) {
        return value != null && String.valueOf(userId).equals(String.valueOf(value));
    }

    @PostMapping("/reservations/{id}/cancel")
    public Map<String, Object> cancel(@PathVariable Long id) {
        return service.cancel(id, SecurityUtils.currentUser());
    }

    @PostMapping("/reservations/{id}/complete")
    public Map<String, Object> complete(@PathVariable Long id) {
        return service.complete(id, SecurityUtils.currentUser());
    }
}
