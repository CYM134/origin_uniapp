package com.companyleveltraining.backend.portal.calendar;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.companyleveltraining.backend.common.SecurityUtils;
import com.companyleveltraining.backend.portal.calendar.dto.CalendarEventResponse;
import com.companyleveltraining.backend.portal.calendar.dto.CalendarEventSaveRequest;

/** 我的日历接口：范围日程、近期日程，以及个人事件维护。 */
@RestController
@RequestMapping("/api/portal/calendar")
public class CalendarController {

    private final CalendarService service;

    public CalendarController(CalendarService service) {
        this.service = service;
    }

    @GetMapping("/events")
    public List<CalendarEventResponse> events(@RequestParam String startDate,
                                              @RequestParam String endDate) {
        return service.events(SecurityUtils.currentUserId(), startDate, endDate);
    }

    @GetMapping("/upcoming")
    public List<CalendarEventResponse> upcoming(@RequestParam(defaultValue = "5") int limit) {
        return service.upcoming(SecurityUtils.currentUserId(), Math.min(Math.max(limit, 1), 20));
    }

    @PostMapping("/events")
    public Map<String, Object> create(@Valid @RequestBody CalendarEventSaveRequest req) {
        Long id = service.create(SecurityUtils.currentUserId(), req);
        return Map.of("success", true, "id", id);
    }

    @PutMapping("/events/{id}")
    public Map<String, Object> update(@PathVariable Long id, @Valid @RequestBody CalendarEventSaveRequest req) {
        service.update(id, SecurityUtils.currentUserId(), req);
        return Map.of("success", true);
    }

    @DeleteMapping("/events/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        service.delete(id, SecurityUtils.currentUserId());
        return Map.of("success", true, "message", "日程已删除");
    }
}
