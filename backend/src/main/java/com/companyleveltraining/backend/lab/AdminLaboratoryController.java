package com.companyleveltraining.backend.lab;

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
import com.companyleveltraining.backend.lab.dto.LabResponse;
import com.companyleveltraining.backend.lab.dto.LabSaveRequest;

/**
 * 管理员实验室管理接口，对应 admin-lab-management 页的增删改查。
 */
@RestController
@RequestMapping("/api/admin/laboratories")
public class AdminLaboratoryController {

    private final LaboratoryService service;

    public AdminLaboratoryController(LaboratoryService service) {
        this.service = service;
    }

    @GetMapping
    public List<LabResponse> list(@RequestParam(required = false) String keyword) {
        SecurityUtils.requireRole("admin");
        return service.list(keyword, false);
    }

    @GetMapping("/{id}")
    public LabResponse get(@PathVariable Long id) {
        SecurityUtils.requireRole("admin");
        return service.get(id);
    }

    @PostMapping
    public LabResponse create(@Valid @RequestBody LabSaveRequest req) {
        SecurityUtils.requireRole("admin");
        return service.create(req, SecurityUtils.currentUserId());
    }

    @PutMapping("/{id}")
    public LabResponse update(@PathVariable Long id, @Valid @RequestBody LabSaveRequest req) {
        SecurityUtils.requireRole("admin");
        return service.update(id, req, SecurityUtils.currentUserId());
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        SecurityUtils.requireRole("admin");
        service.delete(id, SecurityUtils.currentUserId());
        return Map.of("success", true, "message", "实验室已删除");
    }
}
