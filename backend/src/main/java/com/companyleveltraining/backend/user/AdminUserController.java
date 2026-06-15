package com.companyleveltraining.backend.user;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.companyleveltraining.backend.common.SecurityUtils;

/**
 * 管理员用户管理接口，对应 admin-system-management「用户管理」Tab。
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService service;

    public AdminUserController(AdminUserService service) {
        this.service = service;
    }

    @GetMapping
    public List<Map<String, Object>> list(@RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) String role) {
        SecurityUtils.requireRole("admin");
        return service.list(keyword, role);
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, Object> body) {
        SecurityUtils.requireRole("admin");
        return service.create(body, SecurityUtils.currentUserId());
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        SecurityUtils.requireRole("admin");
        return service.update(id, body, SecurityUtils.currentUserId());
    }

    @PatchMapping("/{id}/status")
    public Map<String, Object> toggleStatus(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        SecurityUtils.requireRole("admin");
        String status = body == null ? null : body.get("status");
        return service.toggleStatus(id, status, SecurityUtils.currentUserId());
    }

    @PostMapping("/{id}/reset-password")
    public Map<String, Object> resetPassword(@PathVariable Long id) {
        SecurityUtils.requireRole("admin");
        service.resetPassword(id, SecurityUtils.currentUserId());
        return Map.of("success", true, "message", "密码已重置为 123456");
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        SecurityUtils.requireRole("admin");
        service.delete(id, SecurityUtils.currentUserId());
        return Map.of("success", true, "message", "用户已删除");
    }
}
