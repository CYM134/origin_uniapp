package com.companyleveltraining.backend.reservation;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 预约/审批列表查询 MyBatis 试点。
 * 写操作仍保留在 ReservationRepository 中，以便继续使用现有事务和 KeyHolder 逻辑。
 */
@Mapper
public interface ReservationMapper {

    String BASE_SELECT = """
        SELECT
          ra.id,
          ra.application_no AS applicationNo,
          ra.applicant_role AS applicantRole,
          ra.applicant_user_id AS applicantUserId,
          ra.applicant_no AS applicantNo,
          ra.applicant_no AS studentId,
          ra.applicant_name AS applicant,
          ra.applicant_name AS applicantName,
          ra.applicant_name AS studentName,
          ra.applicant_name AS teacherName,
          ra.applicant_phone AS contact,
          ra.applicant_phone AS phone,
          ra.applicant_phone AS applicantPhone,
          ra.lab_id AS labId,
          ra.lab_name_snapshot AS labName,
          ra.schedule_id AS scheduleId,
          ra.reserve_date AS date,
          ra.start_time AS startTime,
          ra.end_time AS endTime,
          ra.time_slot_label AS timeSlot,
          ra.participant_count AS studentCount,
          ra.application_type AS applicationType,
          ra.application_type_name AS applicationTypeName,
          ra.title,
          ra.purpose,
          ra.instructor_name AS teacher,
          ra.requirements,
          ra.emergency_contact_name AS emergencyContact,
          ra.emergency_contact_phone AS emergencyPhone,
          ra.course_name AS courseName,
          ra.course_type AS courseType,
          ra.remark,
          ra.status,
          CASE ra.status
            WHEN 'draft' THEN '草稿'
            WHEN 'pending' THEN '待审核'
            WHEN 'teacher_approved' THEN '待管理员审核'
            WHEN 'approved' THEN '已通过'
            WHEN 'rejected' THEN '已拒绝'
            WHEN 'cancelled' THEN '已取消'
            WHEN 'completed' THEN '已完成'
            ELSE ra.status END AS statusText,
          CASE WHEN ra.applicant_role = 'student' THEN '学生' ELSE '教师' END AS applicantType,
          ra.is_completed AS isCompleted,
          ra.submitted_at AS submitTime,
          ra.submitted_at AS applyTime,
          ra.teacher_review_user_id AS teacherReviewerId,
          ra.teacher_review_name AS teacherReviewerName,
          ra.teacher_review_at AS teacherReviewTime,
          ra.teacher_review_comment AS teacherReviewReason,
          ra.admin_review_user_id AS adminReviewerId,
          ra.admin_review_name AS adminReviewerName,
          ra.admin_review_at AS adminReviewTime,
          ra.admin_review_at AS approvalTime,
          ra.admin_review_comment AS adminReviewComment,
          ra.reject_reason AS rejectReason,
          ra.cancelled_at AS cancelTime,
          ra.completed_at AS completedTime,
          ra.created_at AS createTime
        FROM reservation_applications ra
        WHERE ra.deleted_at IS NULL AND ra.status <> 'draft'
        """;

    @Select(BASE_SELECT + " AND ra.applicant_user_id = #{userId} ORDER BY ra.submitted_at DESC, ra.id DESC")
    List<Map<String, Object>> findByApplicant(@Param("userId") Long userId);

    @Select(BASE_SELECT + """
        AND ra.applicant_role = 'student' AND ra.status = 'pending'
        ORDER BY ra.submitted_at ASC
        """)
    List<Map<String, Object>> findStudentPendingForTeacher();

    @Select(BASE_SELECT + " AND ra.teacher_review_user_id = #{teacherUserId} ORDER BY ra.teacher_review_at DESC")
    List<Map<String, Object>> findReviewedByTeacher(@Param("teacherUserId") Long teacherUserId);

    @Select(BASE_SELECT + """
        AND ((ra.applicant_role = 'student' AND ra.status = 'teacher_approved')
          OR (ra.applicant_role = 'teacher' AND ra.status = 'pending'))
        ORDER BY ra.submitted_at ASC
        """)
    List<Map<String, Object>> findAdminPending();

    @Select(BASE_SELECT + " AND ra.admin_review_user_id = #{adminUserId} ORDER BY ra.admin_review_at DESC")
    List<Map<String, Object>> findReviewedByAdmin(@Param("adminUserId") Long adminUserId);

    @Select(BASE_SELECT + " ORDER BY ra.submitted_at DESC, ra.id DESC")
    List<Map<String, Object>> findAllForAdmin();

    @Select(BASE_SELECT + """
        AND ra.status IN ('approved','rejected')
        ORDER BY ra.admin_review_at DESC, ra.id DESC
        """)
    List<Map<String, Object>> findApprovalRecords();

    @Select(BASE_SELECT + " AND ra.id = #{id} LIMIT 1")
    Map<String, Object> findById(@Param("id") Long id);
}
