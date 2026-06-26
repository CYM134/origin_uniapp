package com.companyleveltraining.backend.portal.home;

import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.companyleveltraining.backend.common.SecurityUtils;

/** 师生门户首页接口：根据当前用户角色返回聚合首页数据。 */
@RestController
@RequestMapping("/api/portal")
@Tag(name = "综合服务门户", description = "师生综合服务门户首页聚合数据接口")
public class PortalHomeController {

    private final PortalHomeService service;

    public PortalHomeController(PortalHomeService service) {
        this.service = service;
    }

    @GetMapping("/home")
    @Operation(summary = "门户首页", description = "根据当前登录用户角色返回应用、通知、资讯、日程和待办等首页数据")
    public Map<String, Object> home() {
        return service.home(SecurityUtils.currentUser());
    }
}
