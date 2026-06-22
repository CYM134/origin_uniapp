package com.companyleveltraining.backend.lab;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 实验室可预约时间接口：返回标准时间段及其在指定日期是否可约（已被占用则不可约）。
 * 时间段为平台统一的标准节次，后续如需实验室自定义可预约时间，可扩展为独立表。
 */
@RestController
@RequestMapping("/api/laboratories")
public class AvailableTimeController {

    /** 标准时间段：label / start / end。 */
    private static final String[][] SLOTS = {
        {"08:00-10:00", "08:00:00", "10:00:00"},
        {"10:00-12:00", "10:00:00", "12:00:00"},
        {"14:00-16:00", "14:00:00", "16:00:00"},
        {"16:00-18:00", "16:00:00", "18:00:00"},
        {"19:00-21:00", "19:00:00", "21:00:00"}
    };

    private final JdbcTemplate jdbcTemplate;

    public AvailableTimeController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/{id}/available-times")
    public List<Map<String, Object>> availableTimes(@PathVariable Long id, @RequestParam String date) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (String[] slot : SLOTS) {
            Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM reservation_applications
                WHERE lab_id = ? AND reserve_date = ? AND deleted_at IS NULL
                  AND status IN ('pending','teacher_approved','approved')
                  AND start_time < ? AND end_time > ?
                """, Integer.class, id, date, slot[2], slot[1]);
            boolean available = count == null || count == 0;
            result.add(Map.of(
                "timeSlot", slot[0],
                "startTime", slot[1],
                "endTime", slot[2],
                "available", available
            ));
        }
        return result;
    }
}
