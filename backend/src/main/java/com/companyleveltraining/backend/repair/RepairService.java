package com.companyleveltraining.backend.repair;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.companyleveltraining.backend.audit.AuditLogService;
import com.companyleveltraining.backend.common.BizNoGenerator;
import com.companyleveltraining.backend.common.BusinessException;
import com.companyleveltraining.backend.lab.LaboratoryRepository;
import com.companyleveltraining.backend.lab.dto.LabResponse;
import com.companyleveltraining.backend.notification.NotificationService;
import com.companyleveltraining.backend.repair.dto.RepairCreateRequest;
import com.companyleveltraining.backend.security.SecurityUser;

/** 后勤报修：师生提交，管理员单级审核。 */
@Service
public class RepairService {

    private static final Map<String, String> FAULT_TYPE_NAMES = Map.of(
        "equipment", "设备故障",
        "network", "网络故障",
        "environment", "环境问题",
        "safety", "安全隐患",
        "other", "其他"
    );

    private final JdbcTemplate jdbcTemplate;
    private final LaboratoryRepository laboratoryRepository;
    private final BizNoGenerator bizNoGenerator;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    public RepairService(JdbcTemplate jdbcTemplate, LaboratoryRepository laboratoryRepository,
                         BizNoGenerator bizNoGenerator, AuditLogService auditLogService,
                         NotificationService notificationService) {
        this.jdbcTemplate = jdbcTemplate;
        this.laboratoryRepository = laboratoryRepository;
        this.bizNoGenerator = bizNoGenerator;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
    }

    @Transactional
    public Map<String, Object> create(SecurityUser user, RepairCreateRequest req) {
        if (!"student".equals(user.role()) && !"teacher".equals(user.role())) {
            throw BusinessException.forbidden("只有学生和教师可以提交报修");
        }
        LabResponse lab = laboratoryRepository.findById(req.labId())
            .orElseThrow(() -> BusinessException.notFound("所选实验室不存在"));
        if ("deleted".equals(lab.status()) || "disabled".equals(lab.status())) {
            throw BusinessException.conflict("该实验室当前不可报修");
        }

        String repairNo = bizNoGenerator.generate("RR");
        String faultType = normalizeFaultType(req.faultType());
        String urgency = normalizeUrgency(req.urgency());
        jdbcTemplate.update("""
            INSERT INTO repair_requests
              (repair_no, applicant_user_id, applicant_role, applicant_no, applicant_name, applicant_phone,
               lab_id, lab_name_snapshot, lab_location_snapshot, fault_type, urgency, title, description,
               contact_name, contact_phone, preferred_time, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'pending')
            """,
            repairNo, user.id(), user.role(), user.accountNo(), user.realName(), user.phone(),
            req.labId(), lab.name(), lab.location(), faultType, urgency, req.title().trim(),
            req.description().trim(), blankToNull(req.contactName()), blankToNull(req.contactPhone()),
            blankToNull(req.preferredTime()));

        Map<String, Object> detail = getByRepairNo(repairNo);
        Long id = toLong(detail.get("id"));
        notificationService.send(user.id(), user.role(), user.accountNo(), "报修申请已提交",
            "您的报修申请（" + lab.name() + "）已提交，等待管理员审核。", "info", id);
        auditLogService.record(user.id(), user.role(), "repair", "submit", "repair_requests", id, null);
        return detail;
    }

    public List<Map<String, Object>> listMine(Long userId) {
        return jdbcTemplate.queryForList(baseSelect() + """
            WHERE rr.deleted_at IS NULL AND rr.applicant_user_id = ?
            ORDER BY rr.submitted_at DESC
            """, userId);
    }

    public Map<String, Object> myStats(Long userId) {
        List<Map<String, Object>> list = listMine(userId);
        long pending = count(list, "pending");
        long approved = count(list, "approved");
        long rejected = count(list, "rejected");
        long cancelled = count(list, "cancelled");
        return Map.of(
            "total", list.size(),
            "pending", pending,
            "approved", approved,
            "rejected", rejected,
            "cancelled", cancelled,
            "todo", pending
        );
    }

    public List<Map<String, Object>> listForAdmin(String status) {
        if (status == null || status.isBlank()) {
            return jdbcTemplate.queryForList(baseSelect() + """
                WHERE rr.deleted_at IS NULL
                ORDER BY FIELD(rr.status, 'pending', 'approved', 'rejected', 'cancelled'), rr.submitted_at DESC
                """);
        }
        return jdbcTemplate.queryForList(baseSelect() + """
            WHERE rr.deleted_at IS NULL AND rr.status = ?
            ORDER BY rr.submitted_at DESC
            """, normalizeStatusFilter(status));
    }

    public Map<String, Object> detail(Long id, SecurityUser user) {
        Map<String, Object> detail = getById(id);
        if (!"admin".equals(user.role()) && !String.valueOf(user.id()).equals(String.valueOf(detail.get("applicantUserId")))) {
            throw BusinessException.forbidden("无权查看他人的报修申请");
        }
        return detail;
    }

    @Transactional
    public Map<String, Object> cancel(Long id, SecurityUser user) {
        Map<String, Object> detail = detail(id, user);
        if (!"pending".equals(detail.get("status"))) {
            throw BusinessException.conflict("只有待审核报修可以取消");
        }
        int rows = jdbcTemplate.update("""
            UPDATE repair_requests
            SET status = 'cancelled'
            WHERE id = ? AND applicant_user_id = ? AND status = 'pending' AND deleted_at IS NULL
            """, id, user.id());
        if (rows == 0) {
            throw BusinessException.conflict("操作冲突，请刷新后重试");
        }
        auditLogService.record(user.id(), user.role(), "repair", "cancel", "repair_requests", id, null);
        return getById(id);
    }

    @Transactional
    public Map<String, Object> approve(Long id, SecurityUser admin, String comment) {
        return review(id, admin, "approved", comment, null);
    }

    @Transactional
    public Map<String, Object> reject(Long id, SecurityUser admin, String reason) {
        if (reason == null || reason.isBlank()) {
            throw BusinessException.badRequest("请输入驳回原因");
        }
        return review(id, admin, "rejected", null, reason);
    }

    public long countPending() {
        Long count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM repair_requests WHERE deleted_at IS NULL AND status = 'pending'",
            Long.class);
        return count == null ? 0L : count;
    }

    private Map<String, Object> review(Long id, SecurityUser admin, String nextStatus, String comment, String reason) {
        Map<String, Object> before = getById(id);
        if (!"pending".equals(before.get("status"))) {
            throw BusinessException.conflict("只有待审核报修可以处理");
        }
        int rows = jdbcTemplate.update("""
            UPDATE repair_requests
            SET status = ?, admin_review_user_id = ?, admin_review_name = ?, admin_review_at = CURRENT_TIMESTAMP(3),
                admin_comment = ?, reject_reason = ?
            WHERE id = ? AND status = 'pending' AND deleted_at IS NULL
            """, nextStatus, admin.id(), admin.realName(), blankToNull(comment), blankToNull(reason), id);
        if (rows == 0) {
            throw BusinessException.conflict("操作冲突，请刷新后重试");
        }

        Map<String, Object> detail = getById(id);
        Long applicantUserId = toLong(detail.get("applicantUserId"));
        String title = "approved".equals(nextStatus) ? "报修审核已通过" : "报修申请已驳回";
        String content = "approved".equals(nextStatus)
            ? "您的报修申请（" + detail.get("labName") + "）已通过，管理员将安排处理。"
            : "您的报修申请（" + detail.get("labName") + "）已驳回，原因：" + reason;
        notificationService.send(applicantUserId, String.valueOf(detail.get("applicantRole")),
            String.valueOf(detail.get("applicantNo")), title, content, "info", id);
        auditLogService.record(admin.id(), "admin", "repair", nextStatus, "repair_requests", id, null);
        return detail;
    }

    private Map<String, Object> getByRepairNo(String repairNo) {
        return jdbcTemplate.queryForList(baseSelect() + " WHERE rr.repair_no = ? LIMIT 1", repairNo)
            .stream().findFirst().map(LinkedHashMap::new)
            .orElseThrow(() -> BusinessException.notFound("报修申请不存在"));
    }

    private Map<String, Object> getById(Long id) {
        return jdbcTemplate.queryForList(baseSelect() + " WHERE rr.id = ? AND rr.deleted_at IS NULL LIMIT 1", id)
            .stream().findFirst().map(LinkedHashMap::new)
            .orElseThrow(() -> BusinessException.notFound("报修申请不存在"));
    }

    private String baseSelect() {
        return """
            SELECT
              rr.id,
              rr.repair_no AS repairNo,
              rr.applicant_user_id AS applicantUserId,
              rr.applicant_role AS applicantRole,
              rr.applicant_no AS applicantNo,
              rr.applicant_name AS applicantName,
              rr.applicant_phone AS applicantPhone,
              rr.lab_id AS labId,
              rr.lab_name_snapshot AS labName,
              rr.lab_location_snapshot AS labLocation,
              rr.fault_type AS faultType,
              CASE rr.fault_type
                WHEN 'equipment' THEN '设备故障'
                WHEN 'network' THEN '网络故障'
                WHEN 'environment' THEN '环境问题'
                WHEN 'safety' THEN '安全隐患'
                ELSE '其他' END AS faultTypeText,
              rr.urgency,
              CASE rr.urgency
                WHEN 'urgent' THEN '紧急'
                WHEN 'critical' THEN '非常紧急'
                ELSE '普通' END AS urgencyText,
              rr.title,
              rr.description,
              rr.contact_name AS contactName,
              rr.contact_phone AS contactPhone,
              rr.preferred_time AS preferredTime,
              rr.status,
              CASE rr.status
                WHEN 'pending' THEN '待审核'
                WHEN 'approved' THEN '已通过'
                WHEN 'rejected' THEN '已驳回'
                WHEN 'cancelled' THEN '已取消'
                ELSE rr.status END AS statusText,
              rr.admin_review_user_id AS adminReviewUserId,
              rr.admin_review_name AS adminReviewName,
              DATE_FORMAT(rr.admin_review_at, '%Y-%m-%d %H:%i:%s') AS adminReviewAt,
              rr.admin_comment AS adminComment,
              rr.reject_reason AS rejectReason,
              DATE_FORMAT(rr.submitted_at, '%Y-%m-%d %H:%i:%s') AS submittedAt
            FROM repair_requests rr
            """;
    }

    private String normalizeFaultType(String value) {
        if (value == null || value.isBlank()) {
            return "other";
        }
        String v = value.trim();
        if (FAULT_TYPE_NAMES.containsKey(v)) {
            return v;
        }
        return switch (v) {
            case "设备故障" -> "equipment";
            case "网络故障" -> "network";
            case "环境问题" -> "environment";
            case "安全隐患" -> "safety";
            default -> "other";
        };
    }

    private String normalizeUrgency(String value) {
        if (value == null || value.isBlank()) {
            return "normal";
        }
        return switch (value.trim()) {
            case "urgent", "紧急" -> "urgent";
            case "critical", "非常紧急" -> "critical";
            default -> "normal";
        };
    }

    private String normalizeStatusFilter(String value) {
        return switch (value.trim()) {
            case "approved", "已通过" -> "approved";
            case "rejected", "已驳回" -> "rejected";
            case "cancelled", "已取消" -> "cancelled";
            default -> "pending";
        };
    }

    private long count(List<Map<String, Object>> list, String status) {
        return list.stream().filter(m -> status.equals(m.get("status"))).count();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private Long toLong(Object value) {
        if (value instanceof Number n) {
            return n.longValue();
        }
        return value == null ? null : Long.parseLong(String.valueOf(value));
    }
}
