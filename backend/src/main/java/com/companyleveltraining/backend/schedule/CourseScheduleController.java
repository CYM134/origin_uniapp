package com.companyleveltraining.backend.schedule;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 课表预览接口（学生/教师共用），对应 student-schedule-preview / teacher-schedule-preview 的日视图与周视图。
 */
@RestController
@RequestMapping("/api/course-schedules")
public class CourseScheduleController {

    private final ScheduleService service;

    public CourseScheduleController(ScheduleService service) {
        this.service = service;
    }

    @GetMapping
    public List<Map<String, Object>> byDate(@RequestParam String date,
                                            @RequestParam(required = false) Long labId) {
        return service.findByDate(date, labId);
    }

    @GetMapping("/week")
    public List<Map<String, Object>> byWeek(@RequestParam String startDate,
                                            @RequestParam String endDate,
                                            @RequestParam(required = false) Long labId) {
        return service.findByRange(startDate, endDate, labId);
    }

    @GetMapping("/{id}")
    public Map<String, Object> detail(@PathVariable Long id) {
        return service.findById(id);
    }
}
