package com.companyleveltraining.backend.portal.app;

import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.companyleveltraining.backend.common.SecurityUtils;
import com.companyleveltraining.backend.portal.app.dto.AppCategoryResponse;
import com.companyleveltraining.backend.portal.app.dto.AppResponse;
import com.companyleveltraining.backend.security.SecurityUser;

/**
 * 应用中心公共接口：当前登录用户按角色查看可见应用、分类、收藏与访问记录。
 */
@RestController
@RequestMapping("/api/portal")
public class PortalAppController {

    private final PortalAppService service;

    public PortalAppController(PortalAppService service) {
        this.service = service;
    }

    @GetMapping("/app-categories")
    public List<AppCategoryResponse> categories() {
        return service.categories();
    }

    @GetMapping("/apps")
    public List<AppResponse> apps(@RequestParam(required = false) Long categoryId,
                                  @RequestParam(required = false) String keyword) {
        SecurityUser user = SecurityUtils.currentUser();
        return service.visibleApps(user.role(), user.id(), categoryId, keyword);
    }

    @GetMapping("/apps/favorites")
    public List<AppResponse> favorites() {
        SecurityUser user = SecurityUtils.currentUser();
        return service.favorites(user.role(), user.id());
    }

    @PostMapping("/apps/{id}/favorite")
    public Map<String, Object> favorite(@PathVariable Long id) {
        SecurityUser user = SecurityUtils.currentUser();
        boolean favorite = service.addFavorite(user.role(), user.id(), id);
        return Map.of("success", true, "favorite", favorite);
    }

    @DeleteMapping("/apps/{id}/favorite")
    public Map<String, Object> unfavorite(@PathVariable Long id) {
        SecurityUser user = SecurityUtils.currentUser();
        boolean favorite = service.removeFavorite(user.id(), id);
        return Map.of("success", true, "favorite", favorite);
    }

    @PostMapping("/apps/{id}/visit")
    public Map<String, Object> visit(@PathVariable Long id,
                                     @RequestBody(required = false) Map<String, Object> body,
                                     HttpServletRequest request) {
        SecurityUser user = SecurityUtils.currentUser();
        String clientType = body == null ? null : (String) body.get("clientType");
        Long visitCount = service.recordVisit(user.role(), user.id(), id, clientType, request.getRemoteAddr());
        return Map.of("success", true, "visitCount", visitCount == null ? 0 : visitCount);
    }
}
