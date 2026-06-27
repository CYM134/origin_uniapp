package com.companyleveltraining.backend.ai;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * AI 助手业务上下文查询。
 * 只查询回答前需要的少量摘要数据，不做工具调用或 RAG。
 */
@Mapper
public interface AiContextMapper {

    @Select("""
        SELECT COUNT(*) FROM reservation_applications
        WHERE deleted_at IS NULL AND (
          (applicant_role = 'student' AND status = 'teacher_approved')
          OR (applicant_role = 'teacher' AND status = 'pending'))
        """)
    long countAdminPendingReservations();

    @Select("""
        SELECT application_no AS applicationNo, applicant_name AS applicantName,
               lab_name_snapshot AS labName, reserve_date AS reserveDate,
               TIME_FORMAT(start_time, '%H:%i') AS startTime,
               TIME_FORMAT(end_time, '%H:%i') AS endTime,
               status
        FROM reservation_applications
        WHERE deleted_at IS NULL AND status <> 'draft'
        ORDER BY submitted_at DESC, id DESC
        LIMIT 5
        """)
    List<Map<String, Object>> findRecentReservationsForAdmin();

    @Select("""
        SELECT application_no AS applicationNo, lab_name_snapshot AS labName, reserve_date AS reserveDate,
               TIME_FORMAT(start_time, '%H:%i') AS startTime,
               TIME_FORMAT(end_time, '%H:%i') AS endTime,
               status
        FROM reservation_applications
        WHERE deleted_at IS NULL AND status <> 'draft' AND applicant_user_id = #{userId}
        ORDER BY submitted_at DESC, id DESC
        LIMIT 5
        """)
    List<Map<String, Object>> findRecentReservationsByApplicant(@Param("userId") Long userId);

    @Select("""
        SELECT COUNT(*) FROM reservation_applications
        WHERE deleted_at IS NULL AND applicant_user_id = #{userId}
          AND status IN ('pending','teacher_approved','approved')
        """)
    long countActiveReservationsByApplicant(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM teacher_registration_applications WHERE status = 'pending'")
    long countPendingTeacherRegistrations();

    @Select("""
        SELECT COUNT(*) FROM reservation_applications
        WHERE deleted_at IS NULL AND applicant_role = 'student' AND status = 'pending'
        """)
    long countStudentPendingTeacherReviews();

    @Select("""
        SELECT COUNT(*) FROM reservation_applications
        WHERE deleted_at IS NULL AND applicant_user_id = #{userId}
          AND status IN ('pending','teacher_approved')
        """)
    long countPendingReservationsByApplicant(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM notifications WHERE recipient_user_id = #{userId} AND is_read = 0")
    long countUnreadMessages(@Param("userId") Long userId);

    @Select("""
        SELECT title, content, is_read AS isRead,
               DATE_FORMAT(created_at, '%Y-%m-%d %H:%i') AS createTime
        FROM notifications
        WHERE recipient_user_id = #{userId}
        ORDER BY created_at DESC
        LIMIT 3
        """)
    List<Map<String, Object>> findRecentMessages(@Param("userId") Long userId);

    @Select("""
        SELECT title, location,
               DATE_FORMAT(start_time, '%Y-%m-%d %H:%i') AS startTime,
               DATE_FORMAT(end_time, '%Y-%m-%d %H:%i') AS endTime
        FROM calendar_event
        WHERE user_id = #{userId} AND start_time >= CURRENT_TIMESTAMP(3)
        ORDER BY start_time ASC
        LIMIT 3
        """)
    List<Map<String, Object>> findUpcomingCalendarEvents(@Param("userId") Long userId);

    @Select("""
        SELECT CONCAT(lab_name_snapshot, ' · ',
               CASE status
                 WHEN 'pending' THEN '待审核'
                 WHEN 'teacher_approved' THEN '待管理员审核'
                 WHEN 'approved' THEN '已通过'
                 ELSE status END) AS title,
               lab_name_snapshot AS location,
               DATE_FORMAT(CONCAT(reserve_date, ' ', start_time), '%Y-%m-%d %H:%i') AS startTime
        FROM reservation_applications
        WHERE applicant_user_id = #{userId} AND deleted_at IS NULL
          AND status IN ('pending','teacher_approved','approved')
          AND CONCAT(reserve_date, ' ', end_time) >= CURRENT_TIMESTAMP(3)
        ORDER BY reserve_date ASC, start_time ASC
        LIMIT 3
        """)
    List<Map<String, Object>> findUpcomingReservationEvents(@Param("userId") Long userId);

    @Select("""
        SELECT title, publisher_name AS publisherName,
               DATE_FORMAT(publish_time, '%Y-%m-%d %H:%i') AS publishTime
        FROM notice
        WHERE status = 'published' AND deleted_at IS NULL
          AND (publish_scope = 'all' OR FIND_IN_SET(#{role}, target_roles) > 0)
        ORDER BY is_top DESC, publish_time DESC, id DESC
        LIMIT 3
        """)
    List<Map<String, Object>> findVisibleNotices(@Param("role") String role);

    @Select("""
        SELECT cs.course_name AS courseName, l.name AS labName,
               COALESCE(t.real_name, cs.teacher_name_snapshot) AS teacherName,
               DATE_FORMAT(cs.schedule_date, '%Y-%m-%d') AS scheduleDate,
               TIME_FORMAT(cs.start_time, '%H:%i') AS startTime,
               TIME_FORMAT(cs.end_time, '%H:%i') AS endTime,
               cs.status
        FROM course_schedules cs
        JOIN laboratories l ON l.id = cs.lab_id
        LEFT JOIN sys_users t ON t.id = cs.teacher_user_id
        WHERE cs.deleted_at IS NULL
          AND cs.schedule_date >= CURDATE()
        ORDER BY cs.schedule_date ASC, cs.start_time ASC
        LIMIT 5
        """)
    List<Map<String, Object>> findUpcomingSchedulesForAdmin();

    @Select("""
        SELECT cs.course_name AS courseName, l.name AS labName,
               COALESCE(t.real_name, cs.teacher_name_snapshot) AS teacherName,
               DATE_FORMAT(cs.schedule_date, '%Y-%m-%d') AS scheduleDate,
               TIME_FORMAT(cs.start_time, '%H:%i') AS startTime,
               TIME_FORMAT(cs.end_time, '%H:%i') AS endTime,
               cs.status
        FROM course_schedules cs
        JOIN laboratories l ON l.id = cs.lab_id
        LEFT JOIN sys_users t ON t.id = cs.teacher_user_id
        WHERE cs.deleted_at IS NULL
          AND cs.teacher_user_id = #{userId}
          AND cs.schedule_date >= CURDATE()
        ORDER BY cs.schedule_date ASC, cs.start_time ASC
        LIMIT 5
        """)
    List<Map<String, Object>> findUpcomingSchedulesForTeacher(@Param("userId") Long userId);

    @Select("""
        SELECT cs.course_name AS courseName, l.name AS labName,
               COALESCE(t.real_name, cs.teacher_name_snapshot) AS teacherName,
               DATE_FORMAT(cs.schedule_date, '%Y-%m-%d') AS scheduleDate,
               TIME_FORMAT(cs.start_time, '%H:%i') AS startTime,
               TIME_FORMAT(cs.end_time, '%H:%i') AS endTime,
               cs.status
        FROM course_schedule_students css
        JOIN course_schedules cs ON cs.id = css.schedule_id
        JOIN laboratories l ON l.id = cs.lab_id
        LEFT JOIN sys_users t ON t.id = cs.teacher_user_id
        WHERE cs.deleted_at IS NULL
          AND css.student_user_id = #{userId}
          AND cs.schedule_date >= CURDATE()
        ORDER BY cs.schedule_date ASC, cs.start_time ASC
        LIMIT 5
        """)
    List<Map<String, Object>> findUpcomingSchedulesForStudent(@Param("userId") Long userId);

    @Select("""
        SELECT id, name
        FROM laboratories
        WHERE deleted_at IS NULL AND status = 'active'
        ORDER BY id ASC
        LIMIT 3
        """)
    List<Map<String, Object>> findActiveLabsForAvailability();

    @Select("""
        SELECT
          (
            SELECT COUNT(*)
            FROM reservation_applications
            WHERE lab_id = #{labId}
              AND reserve_date = #{date}
              AND deleted_at IS NULL
              AND status IN ('pending','teacher_approved','approved')
              AND start_time < #{endTime}
              AND end_time > #{startTime}
          )
          +
          (
            SELECT COUNT(*)
            FROM course_schedules
            WHERE lab_id = #{labId}
              AND schedule_date = #{date}
              AND deleted_at IS NULL
              AND status <> 'cancelled'
              AND start_time < #{endTime}
              AND end_time > #{startTime}
          )
        """)
    long countLabOccupiedSlots(@Param("labId") Long labId,
                               @Param("date") String date,
                               @Param("startTime") String startTime,
                               @Param("endTime") String endTime);
}
