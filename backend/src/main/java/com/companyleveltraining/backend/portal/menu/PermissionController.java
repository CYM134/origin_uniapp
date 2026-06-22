package com.companyleveltraining.backend.portal.menu;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.companyleveltraining.backend.common.SecurityUtils;
import com.companyleveltraining.backend.security.SecurityUser;

/**
 * 当前用户的菜单与按钮权限接口。前端据此渲染入口与按钮。
 * 沿用 /api/auth 前缀，扩展现有认证模块。
 */
@RestController
@RequestMapping("/api/auth")
public class PermissionController {

    private final MenuCatalog menuCatalog;

    public PermissionController(MenuCatalog menuCatalog) {
        this.menuCatalog = menuCatalog;
    }

    @GetMapping("/menus")
    public List<MenuItem> menus() {
        return menuCatalog.menusForRole(SecurityUtils.currentRole());
    }

    @GetMapping("/permissions")
    public Map<String, Object> permissions() {
        SecurityUser user = SecurityUtils.currentUser();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("role", user.role());
        result.put("menus", menuCatalog.menuCodesForRole(user.role()));
        result.put("buttons", menuCatalog.buttonsForRole(user.role()));
        return result;
    }
}
