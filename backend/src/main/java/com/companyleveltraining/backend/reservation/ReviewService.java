package com.companyleveltraining.backend.reservation;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.companyleveltraining.backend.audit.AuditLogService;
import com.companyleveltraining.backend.common.BusinessException;
import com.companyleveltraining.backend.notification.NotificationService;
import com.companyleveltraining.backend.reservation.ReservationRepository.ReservationState;
import com.companyleveltraining.backend.security.SecurityUser;

/**
 * 预约审批业务：教师审核学生申请（通过/驳回/重审），管理员终审（通过/驳回），并写审批流水、发通知、记审计。
 */
@Service
public class ReviewService {

    private final ReservationRepository repository;
    private final ApprovalLogRepository logRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public ReviewService(ReservationRepository repository, ApprovalLogRepository logRepository,
                         NotificationService notificationService, AuditLogService auditLogService) {
        this.repository = repository;
        this.logRepository = logRepository;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    // ---- 教师端 ----

    public List<Map<String, Object>> teacherPending() {
        return repository.findStudentPendingForTeacher();
    }

    public List<Map<String, Object>> teacherReviewed(Long teacherUserId) {
        return repository.findReviewedByTeacher(teacherUserId);
    }

    @Transactional
    public Map<String, Object> teacherApprove(Long id, SecurityUser teacher, String comment) {
        ReservationState state = requireState(id);
        if (!"student".equals(state.applicantRole()) || !"pending".equals(state.status())) {
            throw BusinessException.conflict("该申请当前不可由教师审核");
        }
        if (repository.teacherReview(id, "pending", "teacher_approved", teacher.id(), teacher.realName(), comment, null) == 0) {
            throw BusinessException.conflict("操作冲突，申请状态已变更，请刷新");
        }
        logRepository.insert(id, "teacher", "approve", "pending", "teacher_approved", teacher.id(), teacher.realName(), comment);
        notifyApplicant(state, "教师审核通过", "您的实验室预约已通过教师审核，等待管理员终审。", "success");
        auditLogService.record(teacher.id(), "teacher", "reservation", "approve", "reservation_applications", id, null);
        return detail(id);
    }

    @Transactional
    public Map<String, Object> teacherReject(Long id, SecurityUser teacher, String reason) {
        ReservationState state = requireState(id);
        if (!"student".equals(state.applicantRole()) || !"pending".equals(state.status())) {
            throw BusinessException.conflict("该申请当前不可由教师审核");
        }
        requireReason(reason);
        if (repository.teacherReview(id, "pending", "rejected", teacher.id(), teacher.realName(), reason, reason) == 0) {
            throw BusinessException.conflict("操作冲突，申请状态已变更，请刷新");
        }
        logRepository.insert(id, "teacher", "reject", "pending", "rejected", teacher.id(), teacher.realName(), reason);
        notifyApplicant(state, "预约申请被驳回", "您的实验室预约被教师驳回：" + reason, "warning");
        auditLogService.record(teacher.id(), "teacher", "reservation", "reject", "reservation_applications", id, null);
        return detail(id);
    }

    @Transactional
    public Map<String, Object> teacherReapprove(Long id, SecurityUser teacher, String comment) {
        ReservationState state = requireState(id);
        if (!"student".equals(state.applicantRole()) || !"rejected".equals(state.status())) {
            throw BusinessException.conflict("只有被驳回的学生申请才能重新审核");
        }
        if (repository.teacherReview(id, "rejected", "teacher_approved", teacher.id(), teacher.realName(), comment, null) == 0) {
            throw BusinessException.conflict("操作冲突，申请状态已变更，请刷新");
        }
        logRepository.insert(id, "teacher", "reprocess", "rejected", "teacher_approved", teacher.id(), teacher.realName(), comment);
        notifyApplicant(state, "申请已重新审核通过", "教师已重新审核通过您的预约，等待管理员终审。", "success");
        auditLogService.record(teacher.id(), "teacher", "reservation", "reprocess", "reservation_applications", id, null);
        return detail(id);
    }

    // ---- 管理员端 ----

    @Transactional
    public Map<String, Object> adminApprove(Long id, SecurityUser admin, String comment) {
        ReservationState state = requireState(id);
        String fromStatus = requireAdminReviewable(state);
        if (repository.adminReview(id, fromStatus, "approved", admin.id(), admin.realName(), comment, null) == 0) {
            throw BusinessException.conflict("操作冲突，申请状态已变更，请刷新");
        }
        logRepository.insert(id, "admin", "approve", fromStatus, "approved", admin.id(), admin.realName(), comment);
        notifyApplicant(state, "预约审批通过", "您的实验室预约已通过管理员终审，可按时使用。", "success");
        auditLogService.record(admin.id(), "admin", "reservation", "approve", "reservation_applications", id, null);
        return detail(id);
    }

    @Transactional
    public Map<String, Object> adminReject(Long id, SecurityUser admin, String reason) {
        ReservationState state = requireState(id);
        String fromStatus = requireAdminReviewable(state);
        requireReason(reason);
        if (repository.adminReview(id, fromStatus, "rejected", admin.id(), admin.realName(), reason, reason) == 0) {
            throw BusinessException.conflict("操作冲突，申请状态已变更，请刷新");
        }
        logRepository.insert(id, "admin", "reject", fromStatus, "rejected", admin.id(), admin.realName(), reason);
        notifyApplicant(state, "预约审批未通过", "您的实验室预约被管理员驳回：" + reason, "error");
        auditLogService.record(admin.id(), "admin", "reservation", "reject", "reservation_applications", id, null);
        return detail(id);
    }

    /**
     * 管理员终审的角色细分守卫（#23）：返回允许终审的源状态。
     * - 教师本人申请：允许从 pending 直接终审；
     * - 学生申请：只允许从 teacher_approved 终审，若仍 pending 则视为未经教师初审而拒绝。
     */
    private String requireAdminReviewable(ReservationState state) {
        if ("teacher".equals(state.applicantRole())) {
            if (!"pending".equals(state.status())) {
                throw BusinessException.conflict("该申请当前不可终审");
            }
            return "pending";
        }
        // applicant_role = 'student'
        if ("pending".equals(state.status())) {
            throw BusinessException.conflict("该申请尚未经教师初审");
        }
        if (!"teacher_approved".equals(state.status())) {
            throw BusinessException.conflict("该申请当前不可终审");
        }
        return "teacher_approved";
    }

    private void notifyApplicant(ReservationState state, String title, String content, String type) {
        notificationService.send(state.applicantUserId(), state.applicantRole(), state.applicantNo(),
            title, content, type, state.id());
    }

    private ReservationState requireState(Long id) {
        return repository.findState(id).orElseThrow(() -> BusinessException.notFound("预约申请不存在"));
    }

    private void requireReason(String reason) {
        if (reason == null || reason.isBlank()) {
            throw BusinessException.badRequest("请填写驳回原因");
        }
    }

    private Map<String, Object> detail(Long id) {
        Map<String, Object> detail = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound("预约申请不存在"));
        detail.put("approvalLogs", logRepository.findByApplication(id));
        return detail;
    }
}
