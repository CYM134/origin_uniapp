package com.companyleveltraining.backend.portal.news;

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
import com.companyleveltraining.backend.portal.news.dto.NewsResponse;
import com.companyleveltraining.backend.portal.news.dto.NewsSaveRequest;

/** 管理员校园资讯管理接口。仅 admin 可访问。 */
@RestController
@RequestMapping("/api/admin/news")
public class AdminNewsController {

    private final NewsService service;

    public AdminNewsController(NewsService service) {
        this.service = service;
    }

    @GetMapping
    public List<NewsResponse> list(@RequestParam(required = false) String status,
                                   @RequestParam(required = false) Long categoryId,
                                   @RequestParam(required = false) String keyword) {
        SecurityUtils.requireRole("admin");
        return service.adminList(status, categoryId, keyword);
    }

    @GetMapping("/{id}")
    public NewsResponse get(@PathVariable Long id) {
        SecurityUtils.requireRole("admin");
        return service.adminGet(id);
    }

    @PostMapping
    public NewsResponse create(@Valid @RequestBody NewsSaveRequest req) {
        SecurityUtils.requireRole("admin");
        return service.create(req, SecurityUtils.currentUser());
    }

    @PutMapping("/{id}")
    public NewsResponse update(@PathVariable Long id, @Valid @RequestBody NewsSaveRequest req) {
        SecurityUtils.requireRole("admin");
        return service.update(id, req, SecurityUtils.currentUser());
    }

    @PutMapping("/{id}/status")
    public NewsResponse setStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        SecurityUtils.requireRole("admin");
        String status = body == null ? null : (String) body.get("status");
        return service.setStatus(id, status, SecurityUtils.currentUser());
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        SecurityUtils.requireRole("admin");
        service.delete(id, SecurityUtils.currentUser());
        return Map.of("success", true, "message", "资讯已删除");
    }
}
