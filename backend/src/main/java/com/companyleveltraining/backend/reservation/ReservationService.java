package com.companyleveltraining.backend.reservation;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.companyleveltraining.backend.audit.AuditLogService;
import com.companyleveltraining.backend.common.BizNoGenerator;
import com.companyleveltraining.backend.common.BusinessException;
import com.companyleveltraining.backend.lab.dto.LabResponse;
import com.companyleveltraining.backend.lab.LaboratoryRepository;
import com.companyleveltraining.backend.reservation.dto.StudentReservationRequest;
import com.companyleveltraining.backend.reservation.dto.TeacherReservationRequest;
import com.companyleveltraining.backend.security.SecurityUser;

/**
 * 预约申请业务：创建、本人列表、详情、取消、完成、统计。
 */
@Service
public class ReservationService {

    private static final Map<String, String> TYPE_NAMES = Map.of(
        "course", "课程实验",
        "research", "科研项目",
        "competition", "竞赛培训",
        "activity", "学术活动",
        "other", "其他"
    );

    private final ReservationRepository repository;
    private final ApprovalLogRepository logRepository;
    private final LaboratoryRepository laboratoryRepository;
    private final BizNoGenerator bizNoGenerator;
    private final AuditLogService auditLogService;

    public ReservationService(ReservationRepository repository, ApprovalLogRepository logRepository,
                              LaboratoryRepository laboratoryRepository, BizNoGenerator bizNoGenerator,
                              AuditLogService auditLogService) {
        this.repository = repository;
        this.logRepository = logRepository;
        this.laboratoryRepository = laboratoryRepository;
        this.bizNoGenerator = bizNoGenerator;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public Map<String, Object> createStudentReservation(SecurityUser user, StudentReservationRequest req) {
        LabResponse lab = requireActiveLab(req.labId());
        validateTime(req.startTime(), req.endTime());
        ensureNoConflict(req.labId(), req.date(), req.startTime(), req.endTime());

        String applicationNo = bizNoGenerator.generate("RA");
        String typeName = req.applicationType() == null ? null : TYPE_NAMES.get(req.applicationType());
        String contact = req.contact() != null && !req.contact().isBlank() ? req.contact() : user.phone();

        ReservationRepository.InsertParams params = new ReservationRepository.InsertParams(
            applicationNo, user.id(), "student", user.accountNo(), user.realName(), contact,
            req.labId(), lab.name(), req.date(), req.startTime(), req.endTime(),
            buildTimeSlot(req.startTime(), req.endTime()), req.studentCount(),
            req.applicationType(), typeName, req.title(), req.purpose(), req.teacher(),
            req.requirements(), req.emergencyContact(), req.emergencyPhone(),
            null, null, null
        );
        Long id = repository.insert(params);
        logRepository.insert(id, "system", "submit", null, "pending", user.id(), user.realName(), "学生提交预约申请");
        auditLogService.record(user.id(), "student", "reservation", "submit", "reservation_applications", id, null);
        return getDetail(id);
    }

    @Transactional
    public Map<String, Object> createTeacherReservation(SecurityUser user, TeacherReservationRequest req) {
        LabResponse lab = requireActiveLab(req.labId());
        validateTime(req.startTime(), req.endTime());
        ensureNoConflict(req.labId(), req.date(), req.startTime(), req.endTime());

        String applicationNo = bizNoGenerator.generate("RA");
        ReservationRepository.InsertParams params = new ReservationRepository.InsertParams(
            applicationNo, user.id(), "teacher", user.accountNo(), user.realName(), user.phone(),
            req.labId(), lab.name(), req.date(), req.startTime(), req.endTime(),
            buildTimeSlot(req.startTime(), req.endTime()), req.studentCount(),
            null, null, null, null, null, null, null, null,
            req.courseName(), req.courseType(), req.remark()
        );
        Long id = repository.insert(params);
        logRepository.insert(id, "system", "submit", null, "pending", user.id(), user.realName(), "教师提交预约申请");
        auditLogService.record(user.id(), "teacher", "reservation", "submit", "reservation_applications", id, null);
        return getDetail(id);
    }

    public List<Map<String, Object>> listMine(Long userId) {
        return repository.findByApplicant(userId);
    }

    public Map<String, Object> getDetail(Long id) {
        Map<String, Object> detail = repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound("预约申请不存在"));
        detail.put("approvalLogs", logRepository.findByApplication(id));
        return detail;
    }

    public Map<String, Object> getMyStats(Long userId) {
        List<Map<String, Object>> all = repository.findByApplicant(userId);
        long pending = count(all, "pending");
        long teacherApproved = count(all, "teacher_approved");
        long approved = count(all, "approved");
        long rejected = count(all, "rejected");
        long cancelled = count(all, "cancelled");
        long completed = count(all, "completed");
        return Map.of(
            "total", all.size(),
            "pending", pending,
            "teacherApproved", teacherApproved,
            "approved", approved,
            "rejected", rejected,
            "cancelled", cancelled,
            "completed", completed,
            "todo", pending + teacherApproved
        );
    }

    @Transactional
    public Map<String, Object> cancel(Long id, SecurityUser user) {
        ReservationRepository.ReservationState state = requireOwned(id, user.id());
        if (!"pending".equals(state.status()) && !"teacher_approved".equals(state.status())) {
            throw BusinessException.conflict("当前状态无法取消");
        }
        if (repository.cancel(id, state.status()) == 0) {
            throw BusinessException.conflict("操作冲突，申请状态已变更，请刷新");
        }
        logRepository.insert(id, "system", "cancel", state.status(), "cancelled", user.id(), user.realName(), "申请人取消预约");
        auditLogService.record(user.id(), user.role(), "reservation", "cancel", "reservation_applications", id, null);
        return getDetail(id);
    }

    @Transactional
    public Map<String, Object> complete(Long id, SecurityUser user) {
        ReservationRepository.ReservationState state = requireOwned(id, user.id());
        if (!"approved".equals(state.status())) {
            throw BusinessException.conflict("只有已通过的预约才能标记完成");
        }
        if (repository.complete(id, state.status()) == 0) {
            throw BusinessException.conflict("操作冲突，申请状态已变更，请刷新");
        }
        logRepository.insert(id, "system", "complete", state.status(), "completed", user.id(), user.realName(), "申请人标记使用完成");
        auditLogService.record(user.id(), user.role(), "reservation", "complete", "reservation_applications", id, null);
        return getDetail(id);
    }

    private ReservationRepository.ReservationState requireOwned(Long id, Long userId) {
        ReservationRepository.ReservationState state = repository.findState(id)
            .orElseThrow(() -> BusinessException.notFound("预约申请不存在"));
        if (!userId.equals(state.applicantUserId())) {
            throw BusinessException.forbidden("无权操作他人的预约申请");
        }
        return state;
    }

    private LabResponse requireActiveLab(Long labId) {
        LabResponse lab = laboratoryRepository.findById(labId)
            .orElseThrow(() -> BusinessException.notFound("所选实验室不存在"));
        if (!"active".equals(lab.status())) {
            throw BusinessException.conflict("该实验室当前不可预约（" + lab.statusText() + "）");
        }
        return lab;
    }

    /**
     * 创建路径专用：在同一事务内对同实验室同日期现有行加排他锁后再判冲突，
     * 与插入串行化，消除 TOCTOU 超订（#3）。仅在 @Transactional 的 create 方法内调用。
     */
    private void ensureNoConflict(Long labId, String date, String startTime, String endTime) {
        if (repository.hasTimeConflictForUpdate(labId, date, startTime, endTime)) {
            throw BusinessException.conflict("该实验室在所选时间段已被预约，请更换时间");
        }
    }

    private void validateTime(String startTime, String endTime) {
        if (startTime == null || endTime == null || endTime.compareTo(startTime) <= 0) {
            throw BusinessException.badRequest("结束时间必须晚于开始时间");
        }
    }

    private String buildTimeSlot(String startTime, String endTime) {
        return shortTime(startTime) + "-" + shortTime(endTime);
    }

    private String shortTime(String time) {
        if (time != null && time.length() >= 5) {
            return time.substring(0, 5);
        }
        return time;
    }

    private long count(List<Map<String, Object>> list, String status) {
        return list.stream().filter(m -> status.equals(m.get("status"))).count();
    }
}
