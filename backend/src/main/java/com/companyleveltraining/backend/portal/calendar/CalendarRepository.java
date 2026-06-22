package com.companyleveltraining.backend.portal.calendar;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.companyleveltraining.backend.common.JdbcUtils;
import com.companyleveltraining.backend.portal.calendar.dto.CalendarEventResponse;
import com.companyleveltraining.backend.portal.calendar.dto.CalendarEventSaveRequest;

/** 日历事件数据访问：个人事件 + 由预约派生的事件。 */
@Repository
public class CalendarRepository {

    private static final RowMapper<CalendarEventResponse> EVENT_MAPPER = (rs, i) -> new CalendarEventResponse(
        rs.getLong("id"),
        rs.getString("title"),
        rs.getString("event_type"),
        rs.getString("startTime"),
        rs.getString("endTime"),
        rs.getString("location"),
        rs.getString("remark"),
        rs.getString("related_business_type"),
        JdbcUtils.nullableLong(rs, "related_business_id"),
        true
    );

    private static final RowMapper<CalendarEventResponse> RESERVATION_EVENT_MAPPER = (rs, i) -> new CalendarEventResponse(
        null,
        rs.getString("title"),
        "reservation",
        rs.getString("startTime"),
        rs.getString("endTime"),
        rs.getString("location"),
        rs.getString("remark"),
        "reservation",
        rs.getLong("related_business_id"),
        false
    );

    private final JdbcTemplate jdbcTemplate;

    public CalendarRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<CalendarEventResponse> findStoredInRange(Long userId, String startDate, String endDate) {
        String sql = """
            SELECT id, title, event_type, location, remark, related_business_type, related_business_id,
                   DATE_FORMAT(start_time, '%Y-%m-%d %H:%i:%s') AS startTime,
                   DATE_FORMAT(end_time, '%Y-%m-%d %H:%i:%s') AS endTime
            FROM calendar_event
            WHERE user_id = ? AND DATE(start_time) BETWEEN ? AND ?
            ORDER BY start_time ASC
            """;
        return jdbcTemplate.query(sql, EVENT_MAPPER, userId, startDate, endDate);
    }

    public List<CalendarEventResponse> findReservationEventsInRange(Long userId, String startDate, String endDate) {
        String sql = """
            SELECT id AS related_business_id,
                   CONCAT(COALESCE(lab_name_snapshot, '实验室'), ' · ',
                          CASE status
                            WHEN 'pending' THEN '待审批'
                            WHEN 'teacher_approved' THEN '待终审'
                            WHEN 'approved' THEN '已通过'
                            WHEN 'completed' THEN '已完成'
                            ELSE status END) AS title,
                   lab_name_snapshot AS location,
                   CONCAT('预约编号 ', application_no) AS remark,
                   DATE_FORMAT(CONCAT(reserve_date, ' ', start_time), '%Y-%m-%d %H:%i:%s') AS startTime,
                   DATE_FORMAT(CONCAT(reserve_date, ' ', end_time), '%Y-%m-%d %H:%i:%s') AS endTime
            FROM reservation_applications
            WHERE applicant_user_id = ? AND deleted_at IS NULL
              AND status IN ('pending','teacher_approved','approved','completed')
              AND reserve_date BETWEEN ? AND ?
            ORDER BY reserve_date ASC, start_time ASC
            """;
        return jdbcTemplate.query(sql, RESERVATION_EVENT_MAPPER, userId, startDate, endDate);
    }

    public List<CalendarEventResponse> findUpcomingReservations(Long userId, int limit) {
        String sql = """
            SELECT id AS related_business_id,
                   CONCAT(COALESCE(lab_name_snapshot, '实验室'), ' · ',
                          CASE status
                            WHEN 'pending' THEN '待审批'
                            WHEN 'teacher_approved' THEN '待终审'
                            WHEN 'approved' THEN '已通过'
                            ELSE status END) AS title,
                   lab_name_snapshot AS location,
                   CONCAT('预约编号 ', application_no) AS remark,
                   DATE_FORMAT(CONCAT(reserve_date, ' ', start_time), '%Y-%m-%d %H:%i:%s') AS startTime,
                   DATE_FORMAT(CONCAT(reserve_date, ' ', end_time), '%Y-%m-%d %H:%i:%s') AS endTime
            FROM reservation_applications
            WHERE applicant_user_id = ? AND deleted_at IS NULL
              AND status IN ('pending','teacher_approved','approved')
              AND CONCAT(reserve_date, ' ', end_time) >= CURRENT_TIMESTAMP(3)
            ORDER BY reserve_date ASC, start_time ASC
            LIMIT ?
            """;
        return jdbcTemplate.query(sql, RESERVATION_EVENT_MAPPER, userId, limit);
    }

    public Optional<CalendarEventResponse> findOwnedById(Long id, Long userId) {
        String sql = """
            SELECT id, title, event_type, location, remark, related_business_type, related_business_id,
                   DATE_FORMAT(start_time, '%Y-%m-%d %H:%i:%s') AS startTime,
                   DATE_FORMAT(end_time, '%Y-%m-%d %H:%i:%s') AS endTime
            FROM calendar_event WHERE id = ? AND user_id = ? LIMIT 1
            """;
        return jdbcTemplate.query(sql, EVENT_MAPPER, id, userId).stream().findFirst();
    }

    public Long insert(Long userId, CalendarEventSaveRequest req) {
        String sql = """
            INSERT INTO calendar_event (user_id, title, event_type, start_time, end_time, location, remark)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[] {"id"});
            ps.setObject(1, userId);
            ps.setString(2, req.title());
            ps.setString(3, req.eventType() == null ? "custom" : req.eventType());
            ps.setString(4, req.startTime());
            ps.setString(5, req.endTime());
            ps.setString(6, req.location());
            ps.setString(7, req.remark());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public int update(Long id, Long userId, CalendarEventSaveRequest req) {
        return jdbcTemplate.update("""
            UPDATE calendar_event
            SET title = ?, event_type = COALESCE(?, event_type), start_time = ?, end_time = ?,
                location = ?, remark = ?
            WHERE id = ? AND user_id = ?
            """, req.title(), req.eventType(), req.startTime(), req.endTime(),
            req.location(), req.remark(), id, userId);
    }

    public int delete(Long id, Long userId) {
        return jdbcTemplate.update("DELETE FROM calendar_event WHERE id = ? AND user_id = ?", id, userId);
    }
}
