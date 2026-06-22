package com.companyleveltraining.backend.portal.notice;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.companyleveltraining.backend.common.SecurityUtils;
import com.companyleveltraining.backend.portal.notice.dto.NoticeResponse;
import com.companyleveltraining.backend.security.SecurityUser;

/** 门户通知公告接口：登录用户按角色查看公告、详情、标记已读。 */
@RestController
@RequestMapping("/api/portal/notices")
public class NoticeController {

    private final NoticeService service;

    public NoticeController(NoticeService service) {
        this.service = service;
    }

    @GetMapping
    public List<NoticeResponse> list(@RequestParam(required = false) String keyword) {
        SecurityUser user = SecurityUtils.currentUser();
        return service.portalList(user.role(), user.id(), keyword);
    }

    @GetMapping("/{id}")
    public NoticeResponse detail(@PathVariable Long id) {
        SecurityUser user = SecurityUtils.currentUser();
        return service.portalDetail(user.role(), user.id(), id);
    }

    @PostMapping("/{id}/read")
    public Map<String, Object> read(@PathVariable Long id) {
        SecurityUser user = SecurityUtils.currentUser();
        service.markRead(user.role(), user.id(), id);
        return Map.of("success", true);
    }
}
