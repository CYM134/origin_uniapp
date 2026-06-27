package com.companyleveltraining.backend.repair;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.companyleveltraining.backend.common.SecurityUtils;
import com.companyleveltraining.backend.repair.dto.RepairCreateRequest;
import com.companyleveltraining.backend.repair.dto.RepairReviewRequest;
import com.companyleveltraining.backend.security.SecurityUser;

/** 后勤报修接口：师生提交，管理员审核。 */
@RestController
@RequestMapping("/api")
@Tag(name = "报修服务", description = "师生报修申请、我的报修、管理员报修审核")
public class RepairController {

    private final RepairService service;

    public RepairController(RepairService service) {
        this.service = service;
    }

    @PostMapping("/repairs")
    @Operation(summary = "提交报修")
    public Map<String, Object> create(@Valid @RequestBody RepairCreateRequest req) {
        return service.create(SecurityUtils.currentUser(), req);
    }

    @GetMapping("/repairs/mine")
    @Operation(summary = "我的报修列表")
    public List<Map<String, Object>> mine() {
        return service.listMine(SecurityUtils.currentUserId());
    }

    @GetMapping("/repairs/mine/stats")
    @Operation(summary = "我的报修统计")
    public Map<String, Object> stats() {
        return service.myStats(SecurityUtils.currentUserId());
    }

    @GetMapping("/repairs/{id}")
    @Operation(summary = "报修详情")
    public Map<String, Object> detail(@PathVariable Long id) {
        return service.detail(id, SecurityUtils.currentUser());
    }

    @PostMapping("/repairs/{id}/cancel")
    @Operation(summary = "取消报修")
    public Map<String, Object> cancel(@PathVariable Long id) {
        return service.cancel(id, SecurityUtils.currentUser());
    }

    @GetMapping("/admin/repairs")
    @Operation(summary = "管理员报修列表")
    public List<Map<String, Object>> adminList(@RequestParam(required = false) String status) {
        SecurityUtils.requireRole("admin");
        return service.listForAdmin(status);
    }

    @GetMapping("/admin/repairs/{id}")
    @Operation(summary = "管理员报修详情")
    public Map<String, Object> adminDetail(@PathVariable Long id) {
        SecurityUtils.requireRole("admin");
        return service.detail(id, SecurityUtils.currentUser());
    }

    @PostMapping("/admin/repairs/{id}/approve")
    @Operation(summary = "通过报修")
    public Map<String, Object> approve(@PathVariable Long id, @RequestBody(required = false) RepairReviewRequest req) {
        SecurityUtils.requireRole("admin");
        SecurityUser admin = SecurityUtils.currentUser();
        return service.approve(id, admin, req == null ? null : req.comment());
    }

    @PostMapping("/admin/repairs/{id}/reject")
    @Operation(summary = "驳回报修")
    public Map<String, Object> reject(@PathVariable Long id, @RequestBody(required = false) RepairReviewRequest req) {
        SecurityUtils.requireRole("admin");
        SecurityUser admin = SecurityUtils.currentUser();
        return service.reject(id, admin, req == null ? null : (req.reason() == null ? req.comment() : req.reason()));
    }
}
