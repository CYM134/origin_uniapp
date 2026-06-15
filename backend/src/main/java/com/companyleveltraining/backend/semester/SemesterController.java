package com.companyleveltraining.backend.semester;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.companyleveltraining.backend.common.BusinessException;

/**
 * 学期接口：替换 admin-schedule-management 写死的 semesters 数组，以及课表页对「当前学期」的隐式依赖。
 */
@RestController
@RequestMapping("/api/academic-semesters")
public class SemesterController {

    private static final String BASE_SELECT = """
        SELECT id, semester_code AS semesterCode, semester_name AS semesterName,
               academic_year AS academicYear, term_no AS termNo,
               start_date AS startDate, end_date AS endDate, status
        FROM academic_semesters
        """;

    private final JdbcTemplate jdbcTemplate;

    public SemesterController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return jdbcTemplate.queryForList(BASE_SELECT + " ORDER BY start_date DESC");
    }

    @GetMapping("/current")
    public Map<String, Object> current() {
        List<Map<String, Object>> active = jdbcTemplate.queryForList(
            BASE_SELECT + " WHERE status = 'active' ORDER BY start_date DESC LIMIT 1");
        if (!active.isEmpty()) {
            return active.get(0);
        }
        List<Map<String, Object>> latest = jdbcTemplate.queryForList(
            BASE_SELECT + " ORDER BY start_date DESC LIMIT 1");
        if (latest.isEmpty()) {
            throw BusinessException.notFound("尚未配置学期");
        }
        return latest.get(0);
    }
}
