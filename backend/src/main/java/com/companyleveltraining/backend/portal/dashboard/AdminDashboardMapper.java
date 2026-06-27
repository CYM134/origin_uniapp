package com.companyleveltraining.backend.portal.dashboard;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 管理端工作台统计查询 MyBatis 试点。
 * 当前先承接多处复用的只读统计 SQL，后续复杂列表查询可继续迁移到 Mapper/XML。
 */
@Mapper
public interface AdminDashboardMapper {

    @Select("""
        SELECT COUNT(*) FROM reservation_applications
        WHERE deleted_at IS NULL AND (
          (applicant_role = 'student' AND status = 'teacher_approved')
          OR (applicant_role = 'teacher' AND status = 'pending'))
        """)
    long countPendingReservations();

    @Select("""
        SELECT COUNT(*) FROM reservation_applications
        WHERE deleted_at IS NULL AND applicant_role = 'student' AND status = 'pending'
        """)
    long countPendingTeacherReviews();

    @Select("SELECT COUNT(*) FROM teacher_registration_applications WHERE status = 'pending'")
    long countPendingTeacherRegistrations();

    @Select("SELECT COUNT(*) FROM repair_requests WHERE deleted_at IS NULL AND status = 'pending'")
    long countPendingRepairs();

    @Select("""
        SELECT COUNT(*) FROM reservation_applications
        WHERE deleted_at IS NULL AND reserve_date = CURDATE()
          AND status IN ('pending','teacher_approved','approved','completed')
        """)
    long countTodayReservations();

    @Select("""
        SELECT COUNT(*) FROM reservation_applications
        WHERE deleted_at IS NULL AND YEARWEEK(reserve_date, 1) = YEARWEEK(CURDATE(), 1)
          AND status IN ('pending','teacher_approved','approved','completed')
        """)
    long countWeekReservations();

    @Select("SELECT COUNT(*) FROM laboratories WHERE deleted_at IS NULL")
    long countTotalLabs();

    @Select("SELECT COUNT(*) FROM laboratories WHERE deleted_at IS NULL AND status = 'active'")
    long countActiveLabs();

    @Select("SELECT COUNT(*) FROM sys_users WHERE deleted_at IS NULL AND status <> 'deleted'")
    long countTotalUsers();

    @Select("SELECT COUNT(*) FROM portal_app WHERE status = 1")
    long countTotalApps();

    @Select("SELECT COUNT(*) FROM notice WHERE status = 'published' AND deleted_at IS NULL")
    long countPublishedNotices();

    @Select("""
        SELECT COUNT(*) FROM reservation_applications
        WHERE deleted_at IS NULL AND reserve_date = CURDATE() AND status IN ('approved','completed')
        """)
    long countApprovedToday();

    @Select("""
        SELECT status, COUNT(*) AS count
        FROM reservation_applications
        WHERE deleted_at IS NULL AND status <> 'draft'
        GROUP BY status
        """)
    List<Map<String, Object>> statusDistribution();
}
