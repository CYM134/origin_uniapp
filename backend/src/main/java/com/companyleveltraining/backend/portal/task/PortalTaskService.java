package com.companyleveltraining.backend.portal.task;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.companyleveltraining.backend.reservation.ReservationRepository;
import com.companyleveltraining.backend.reservation.ReservationService;
import com.companyleveltraining.backend.security.SecurityUser;

/**
 * 任务中心：聚合现有预约审批数据，对外提供待办 / 已办 / 我发起的统一视图，不新建 task 表。
 * 第一阶段任务类型仅有"实验室预约审批"。
 */
@Service
public class PortalTaskService {

    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;

    public PortalTaskService(ReservationRepository reservationRepository,
                             ReservationService reservationService) {
        this.reservationRepository = reservationRepository;
        this.reservationService = reservationService;
    }

    /** 我的待办：教师=待初审的学生申请；管理员=待终审申请；学生=无审批待办。 */
    public List<Map<String, Object>> todo(SecurityUser user) {
        List<Map<String, Object>> list = switch (user.role()) {
            case "teacher" -> reservationRepository.findStudentPendingForTeacher();
            case "admin" -> reservationRepository.findAdminPending();
            default -> List.of();
        };
        return decorate(list);
    }

    /** 我的已办：教师/管理员本人审批过的申请。 */
    public List<Map<String, Object>> done(SecurityUser user) {
        List<Map<String, Object>> list = switch (user.role()) {
            case "teacher" -> reservationRepository.findReviewedByTeacher(user.id());
            case "admin" -> reservationRepository.findReviewedByAdmin(user.id());
            default -> List.of();
        };
        return decorate(list);
    }

    /** 我发起的：本人提交的预约申请。 */
    public List<Map<String, Object>> mine(SecurityUser user) {
        return decorate(reservationRepository.findByApplicant(user.id()));
    }

    public Map<String, Object> detail(Long id) {
        return reservationService.getDetail(id);
    }

    /** 任务统计：计数 + 任务类型分布（用于工作台图表）。 */
    public Map<String, Object> stats(SecurityUser user) {
        int todo = todo(user).size();
        int done = done(user).size();
        int mine = mine(user).size();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("todo", todo);
        result.put("done", done);
        result.put("mine", mine);
        result.put("taskTypes", List.of(
            Map.of("type", "lab_reservation", "name", "实验室预约审批", "count", todo + done)
        ));
        return result;
    }

    private List<Map<String, Object>> decorate(List<Map<String, Object>> list) {
        for (Map<String, Object> m : list) {
            m.put("taskType", "lab_reservation");
            m.put("taskTypeName", "实验室预约审批");
            m.put("businessType", "reservation");
            m.put("businessId", m.get("id"));
        }
        return list;
    }
}
