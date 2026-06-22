package com.companyleveltraining.backend.portal.home;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.companyleveltraining.backend.common.SecurityUtils;

/** 师生门户首页接口：根据当前用户角色返回聚合首页数据。 */
@RestController
@RequestMapping("/api/portal")
public class PortalHomeController {

    private final PortalHomeService service;

    public PortalHomeController(PortalHomeService service) {
        this.service = service;
    }

    @GetMapping("/home")
    public Map<String, Object> home() {
        return service.home(SecurityUtils.currentUser());
    }
}
