package com.companyleveltraining.backend.reservation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/**
 * 预约申请仓储。读操作委托 MyBatis Mapper，写操作覆盖创建与各状态流转。
 */
@Repository
public class ReservationRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ReservationMapper reservationMapper;

    public ReservationRepository(JdbcTemplate jdbcTemplate, ReservationMapper reservationMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.reservationMapper = reservationMapper;
    }

    // ---- 读 ----

    public List<Map<String, Object>> findByApplicant(Long userId) {
        return reservationMapper.findByApplicant(userId);
    }

    public List<Map<String, Object>> findStudentPendingForTeacher() {
        return reservationMapper.findStudentPendingForTeacher();
    }

    public List<Map<String, Object>> findReviewedByTeacher(Long teacherUserId) {
        return reservationMapper.findReviewedByTeacher(teacherUserId);
    }

    /** 任务中心：等待管理员终审的申请（学生 teacher_approved + 教师 pending）。 */
    public List<Map<String, Object>> findAdminPending() {
        return reservationMapper.findAdminPending();
    }

    /** 任务中心：由该管理员终审过的申请。 */
    public List<Map<String, Object>> findReviewedByAdmin(Long adminUserId) {
        return reservationMapper.findReviewedByAdmin(adminUserId);
    }

    public List<Map<String, Object>> findAllForAdmin() {
        return reservationMapper.findAllForAdmin();
    }

    public List<Map<String, Object>> findApprovalRecords() {
        return reservationMapper.findApprovalRecords();
    }

    public Optional<Map<String, Object>> findById(Long id) {
        return Optional.ofNullable(reservationMapper.findById(id));
    }

    /** 取流转所需的内部状态信息。 */
    public Optional<ReservationState> findState(Long id) {
        return jdbcTemplate.query("""
            SELECT id, status, applicant_user_id, applicant_role, applicant_no, applicant_name,
                   lab_name_snapshot
            FROM reservation_applications
            WHERE id = ? AND deleted_at IS NULL
            """, (rs, n) -> new ReservationState(
                rs.getLong("id"),
                rs.getString("status"),
                rs.getLong("applicant_user_id"),
                rs.getString("applicant_role"),
                rs.getString("applicant_no"),
                rs.getString("applicant_name"),
                rs.getString("lab_name_snapshot")
            ), id).stream().findFirst();
    }

    /** 时间段冲突检测：同实验室同日期，已占用（待/通过）的预约时间段是否重叠。仅读用，不加锁。 */
    public boolean hasTimeConflict(Long labId, String date, String startTime, String endTime) {
        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*) FROM reservation_applications
            WHERE lab_id = ? AND reserve_date = ? AND deleted_at IS NULL
              AND status IN ('pending','teacher_approved','approved')
              AND start_time < ? AND end_time > ?
            """, Integer.class, labId, date, endTime, startTime);
        return count != null && count > 0;
    }

    /**
     * 创建预约专用的冲突检测：在事务内对同实验室同日期的现有行加排他锁（FOR UPDATE），
     * 串行化并发预约以消除 check-then-act 竞态（#3）。必须在 @Transactional 方法内调用。
     */
    public boolean hasTimeConflictForUpdate(Long labId, String date, String startTime, String endTime) {
        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*) FROM reservation_applications
            WHERE lab_id = ? AND reserve_date = ? AND deleted_at IS NULL
              AND status IN ('pending','teacher_approved','approved')
              AND start_time < ? AND end_time > ?
            FOR UPDATE
            """, Integer.class, labId, date, endTime, startTime);
        return count != null && count > 0;
    }

    // ---- 写 ----

    public Long insert(InsertParams p) {
        String sql = """
            INSERT INTO reservation_applications
              (application_no, applicant_user_id, applicant_role, applicant_no, applicant_name, applicant_phone,
               lab_id, lab_name_snapshot, reserve_date, start_time, end_time, time_slot_label, participant_count,
               application_type, application_type_name, title, purpose, instructor_name, requirements,
               emergency_contact_name, emergency_contact_phone, course_name, course_type, remark,
               status, submitted_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'pending', CURRENT_TIMESTAMP(3))
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[] {"id"});
            ps.setString(1, p.applicationNo());
            ps.setObject(2, p.applicantUserId());
            ps.setString(3, p.applicantRole());
            ps.setString(4, p.applicantNo());
            ps.setString(5, p.applicantName());
            ps.setString(6, p.applicantPhone());
            ps.setObject(7, p.labId());
            ps.setString(8, p.labName());
            ps.setString(9, p.date());
            ps.setString(10, p.startTime());
            ps.setString(11, p.endTime());
            ps.setString(12, p.timeSlot());
            ps.setInt(13, p.participantCount() == null ? 1 : p.participantCount());
            ps.setString(14, p.applicationType());
            ps.setString(15, p.applicationTypeName());
            ps.setString(16, p.title());
            ps.setString(17, p.purpose());
            ps.setString(18, p.instructorName());
            ps.setString(19, p.requirements());
            ps.setString(20, p.emergencyContact());
            ps.setString(21, p.emergencyPhone());
            ps.setString(22, p.courseName());
            ps.setString(23, p.courseType());
            ps.setString(24, p.remark());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public int updateStatus(Long id, String fromStatus, String toStatus) {
        return jdbcTemplate.update("""
            UPDATE reservation_applications SET status = ?
            WHERE id = ? AND status = ? AND deleted_at IS NULL
            """, toStatus, id, fromStatus);
    }

    /** 取消：带 status 守卫的乐观流转，返回受影响行数（0 表示状态已变更，#9）。 */
    public int cancel(Long id, String fromStatus) {
        return jdbcTemplate.update("""
            UPDATE reservation_applications
            SET status = 'cancelled', cancelled_at = CURRENT_TIMESTAMP(3)
            WHERE id = ? AND status = ? AND deleted_at IS NULL
            """, id, fromStatus);
    }

    /** 完成：带 status 守卫的乐观流转，返回受影响行数（0 表示状态已变更，#9）。 */
    public int complete(Long id, String fromStatus) {
        return jdbcTemplate.update("""
            UPDATE reservation_applications
            SET status = 'completed', is_completed = 1, completed_at = CURRENT_TIMESTAMP(3)
            WHERE id = ? AND status = ? AND deleted_at IS NULL
            """, id, fromStatus);
    }

    /** 教师审核：带 status 守卫的乐观流转，审核人/时间/原因合并进同一条 UPDATE，返回受影响行数（#9）。 */
    public int teacherReview(Long id, String fromStatus, String toStatus, Long reviewerId, String reviewerName,
                             String comment, String rejectReason) {
        return jdbcTemplate.update("""
            UPDATE reservation_applications
            SET status = ?, teacher_review_user_id = ?, teacher_review_name = ?,
                teacher_review_at = CURRENT_TIMESTAMP(3), teacher_review_comment = ?,
                reject_reason = ?
            WHERE id = ? AND status = ? AND deleted_at IS NULL
            """, toStatus, reviewerId, reviewerName, comment, rejectReason, id, fromStatus);
    }

    /** 管理员终审：带 status 守卫的乐观流转，审核人/时间/原因合并进同一条 UPDATE，返回受影响行数（#9）。 */
    public int adminReview(Long id, String fromStatus, String toStatus, Long reviewerId, String reviewerName,
                           String comment, String rejectReason) {
        return jdbcTemplate.update("""
            UPDATE reservation_applications
            SET status = ?, admin_review_user_id = ?, admin_review_name = ?,
                admin_review_at = CURRENT_TIMESTAMP(3), admin_review_comment = ?,
                reject_reason = ?
            WHERE id = ? AND status = ? AND deleted_at IS NULL
            """, toStatus, reviewerId, reviewerName, comment, rejectReason, id, fromStatus);
    }

    /** 内部状态流转载体。 */
    public record ReservationState(
        Long id, String status, Long applicantUserId, String applicantRole,
        String applicantNo, String applicantName, String labName
    ) {
    }

    /** 创建预约的入参载体。 */
    public record InsertParams(
        String applicationNo, Long applicantUserId, String applicantRole, String applicantNo,
        String applicantName, String applicantPhone, Long labId, String labName, String date,
        String startTime, String endTime, String timeSlot, Integer participantCount,
        String applicationType, String applicationTypeName, String title, String purpose,
        String instructorName, String requirements, String emergencyContact, String emergencyPhone,
        String courseName, String courseType, String remark
    ) {
    }
}
