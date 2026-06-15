package com.companyleveltraining.backend.notification;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.companyleveltraining.backend.common.SecurityUtils;

/**
 * 站内通知接口，对应学生/教师端的消息通知与未读角标。
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return service.list(SecurityUtils.currentUserId());
    }

    @GetMapping("/unread-count")
    public Map<String, Object> unreadCount() {
        return Map.of("count", service.unreadCount(SecurityUtils.currentUserId()));
    }

    @PostMapping("/{id}/read")
    public Map<String, Object> markRead(@PathVariable Long id) {
        service.markRead(id, SecurityUtils.currentUserId());
        return Map.of("success", true);
    }

    @PostMapping("/read-all")
    public Map<String, Object> markAllRead() {
        service.markAllRead(SecurityUtils.currentUserId());
        return Map.of("success", true);
    }
}
