package com.companyleveltraining.backend.portal.notice;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "通知公告", description = "门户通知公告列表、详情和已读状态接口")
public class NoticeController {

    private final NoticeService service;

    public NoticeController(NoticeService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "通知公告列表", description = "按当前用户角色查询可见通知公告，支持关键字筛选")
    public List<NoticeResponse> list(@RequestParam(required = false) String keyword) {
        SecurityUser user = SecurityUtils.currentUser();
        return service.portalList(user.role(), user.id(), keyword);
    }

    @GetMapping("/{id}")
    @Operation(summary = "通知公告详情", description = "查询当前用户有权限查看的通知公告详情")
    public NoticeResponse detail(@PathVariable Long id) {
        SecurityUser user = SecurityUtils.currentUser();
        return service.portalDetail(user.role(), user.id(), id);
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "标记通知已读", description = "将指定通知公告标记为当前用户已读")
    public Map<String, Object> read(@PathVariable Long id) {
        SecurityUser user = SecurityUtils.currentUser();
        service.markRead(user.role(), user.id(), id);
        return Map.of("success", true);
    }
}
