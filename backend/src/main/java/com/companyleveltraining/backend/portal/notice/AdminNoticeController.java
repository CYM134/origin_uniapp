package com.companyleveltraining.backend.portal.notice;

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
import com.companyleveltraining.backend.portal.notice.dto.NoticeResponse;
import com.companyleveltraining.backend.portal.notice.dto.NoticeSaveRequest;

/** 管理员通知公告管理接口。仅 admin 可访问。 */
@RestController
@RequestMapping("/api/admin/notices")
public class AdminNoticeController {

    private final NoticeService service;

    public AdminNoticeController(NoticeService service) {
        this.service = service;
    }

    @GetMapping
    public List<NoticeResponse> list(@RequestParam(required = false) String status,
                                     @RequestParam(required = false) String keyword) {
        SecurityUtils.requireRole("admin");
        return service.adminList(status, keyword);
    }

    @GetMapping("/{id}")
    public NoticeResponse get(@PathVariable Long id) {
        SecurityUtils.requireRole("admin");
        return service.adminGet(id);
    }

    @PostMapping
    public NoticeResponse create(@Valid @RequestBody NoticeSaveRequest req) {
        SecurityUtils.requireRole("admin");
        return service.create(req, SecurityUtils.currentUser());
    }

    @PutMapping("/{id}")
    public NoticeResponse update(@PathVariable Long id, @Valid @RequestBody NoticeSaveRequest req) {
        SecurityUtils.requireRole("admin");
        return service.update(id, req, SecurityUtils.currentUser());
    }

    @PutMapping("/{id}/status")
    public NoticeResponse setStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        SecurityUtils.requireRole("admin");
        String status = body == null ? null : (String) body.get("status");
        return service.setStatus(id, status, SecurityUtils.currentUser());
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        SecurityUtils.requireRole("admin");
        service.delete(id, SecurityUtils.currentUser());
        return Map.of("success", true, "message", "公告已删除");
    }
}
