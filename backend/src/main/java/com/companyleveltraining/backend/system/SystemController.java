package com.companyleveltraining.backend.system;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.companyleveltraining.backend.common.SecurityUtils;

/**
 * 系统管理接口：系统设置 + 数据备份，对应 admin-system-management 页。
 */
@RestController
@RequestMapping("/api/admin")
public class SystemController {

    private final SystemService service;

    public SystemController(SystemService service) {
        this.service = service;
    }

    @GetMapping("/system-settings")
    public Map<String, Object> getSettings() {
        SecurityUtils.requireRole("admin");
        return service.getSettings();
    }

    @PutMapping("/system-settings")
    public Map<String, Object> updateSettings(@RequestBody Map<String, Object> body) {
        SecurityUtils.requireRole("admin");
        return service.updateSettings(body, SecurityUtils.currentUserId());
    }

    @GetMapping("/backups")
    public List<Map<String, Object>> listBackups() {
        SecurityUtils.requireRole("admin");
        return service.listBackups();
    }

    @PostMapping("/backups")
    public Map<String, Object> createBackup(@RequestBody(required = false) Map<String, String> body) {
        SecurityUtils.requireRole("admin");
        String type = body == null ? null : body.get("type");
        return service.createBackup(type, SecurityUtils.currentUserId());
    }

    @GetMapping("/backups/{id}")
    public Map<String, Object> getBackup(@PathVariable Long id) {
        SecurityUtils.requireRole("admin");
        return service.getBackupById(id);
    }

    @PostMapping("/backups/{id}/restore")
    public Map<String, Object> restoreBackup(@PathVariable Long id) {
        SecurityUtils.requireRole("admin");
        return service.restoreBackup(id);
    }

    @DeleteMapping("/backups/{id}")
    public Map<String, Object> deleteBackup(@PathVariable Long id) {
        SecurityUtils.requireRole("admin");
        service.deleteBackup(id);
        return Map.of("success", true, "message", "备份已删除");
    }
}
