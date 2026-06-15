package com.companyleveltraining.backend.common;

import com.companyleveltraining.backend.security.SecurityUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 从 Spring Security 上下文中取出当前登录用户的工具方法。
 * 所有需要鉴权的接口都已经过 JwtAuthenticationFilter，principal 必为 {@link SecurityUser}。
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static SecurityUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUser user)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "未认证或 token 无效");
        }
        return user;
    }

    public static Long currentUserId() {
        return currentUser().id();
    }

    public static String currentRole() {
        return currentUser().role();
    }

    public static void requireRole(String role) {
        if (!role.equals(currentRole())) {
            throw BusinessException.forbidden("当前用户无权访问该资源");
        }
    }
}
