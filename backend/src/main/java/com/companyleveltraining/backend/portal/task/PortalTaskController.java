package com.companyleveltraining.backend.portal.task;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.companyleveltraining.backend.common.SecurityUtils;

/**
 * 任务中心接口：待办 / 已办 / 我发起的 / 任务统计 / 任务详情。
 * 审批动作仍调用现有 /api/teacher/reservations/* 与 /api/admin/reservations/* 接口。
 */
@RestController
@RequestMapping("/api/portal/tasks")
public class PortalTaskController {

    private final PortalTaskService service;

    public PortalTaskController(PortalTaskService service) {
        this.service = service;
    }

    @GetMapping("/todo")
    public List<Map<String, Object>> todo() {
        return service.todo(SecurityUtils.currentUser());
    }

    @GetMapping("/done")
    public List<Map<String, Object>> done() {
        return service.done(SecurityUtils.currentUser());
    }

    @GetMapping("/mine")
    public List<Map<String, Object>> mine() {
        return service.mine(SecurityUtils.currentUser());
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return service.stats(SecurityUtils.currentUser());
    }

    @GetMapping("/{id}")
    public Map<String, Object> detail(@PathVariable Long id) {
        return service.detail(id);
    }
}
