package com.companyleveltraining.backend.teacherreg;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.companyleveltraining.backend.common.SecurityUtils;

/**
 * 管理员教师注册审核接口，对应 admin-teacher-registration 的三个 tab 与通过/拒绝操作。
 */
@RestController
@RequestMapping("/api/admin/teacher-registrations")
public class TeacherRegistrationController {

    private final TeacherRegistrationService service;

    public TeacherRegistrationController(TeacherRegistrationService service) {
        this.service = service;
    }

    @GetMapping
    public List<Map<String, Object>> list(@RequestParam(required = false) String status) {
        SecurityUtils.requireRole("admin");
        return service.list(status);
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        SecurityUtils.requireRole("admin");
        return service.get(id);
    }

    @PostMapping("/{id}/approve")
    public Map<String, Object> approve(@PathVariable Long id) {
        SecurityUtils.requireRole("admin");
        return service.approve(id, SecurityUtils.currentUserId());
    }

    @PostMapping("/{id}/reject")
    public Map<String, Object> reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        SecurityUtils.requireRole("admin");
        String reason = body == null ? null : body.getOrDefault("reason", body.get("rejectReason"));
        return service.reject(id, SecurityUtils.currentUserId(), reason);
    }
}
