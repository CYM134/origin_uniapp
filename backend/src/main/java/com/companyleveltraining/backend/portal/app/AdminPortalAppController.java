package com.companyleveltraining.backend.portal.app;

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
import com.companyleveltraining.backend.portal.app.dto.AppResponse;
import com.companyleveltraining.backend.portal.app.dto.AppSaveRequest;
import com.companyleveltraining.backend.portal.app.dto.AppSortItem;

/**
 * 管理员应用管理接口：应用增删改查、启停、排序。仅 admin 可访问。
 */
@RestController
@RequestMapping("/api/admin/portal/apps")
public class AdminPortalAppController {

    private final PortalAppService service;

    public AdminPortalAppController(PortalAppService service) {
        this.service = service;
    }

    @GetMapping
    public List<AppResponse> list(@RequestParam(required = false) Long categoryId,
                                  @RequestParam(required = false) String keyword) {
        SecurityUtils.requireRole("admin");
        return service.adminList(categoryId, keyword);
    }

    @GetMapping("/{id}")
    public AppResponse get(@PathVariable Long id) {
        SecurityUtils.requireRole("admin");
        return service.adminGet(id);
    }

    @PostMapping
    public AppResponse create(@Valid @RequestBody AppSaveRequest req) {
        SecurityUtils.requireRole("admin");
        return service.create(req, SecurityUtils.currentUserId());
    }

    @PutMapping("/{id}")
    public AppResponse update(@PathVariable Long id, @Valid @RequestBody AppSaveRequest req) {
        SecurityUtils.requireRole("admin");
        return service.update(id, req, SecurityUtils.currentUserId());
    }

    @PutMapping("/{id}/status")
    public AppResponse setStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        SecurityUtils.requireRole("admin");
        Integer status = body.get("status") == null ? null : ((Number) body.get("status")).intValue();
        return service.setStatus(id, status, SecurityUtils.currentUserId());
    }

    @PutMapping("/sort")
    public Map<String, Object> sort(@RequestBody List<AppSortItem> items) {
        SecurityUtils.requireRole("admin");
        service.updateSort(items, SecurityUtils.currentUserId());
        return Map.of("success", true);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        SecurityUtils.requireRole("admin");
        service.delete(id, SecurityUtils.currentUserId());
        return Map.of("success", true, "message", "应用已删除");
    }
}
