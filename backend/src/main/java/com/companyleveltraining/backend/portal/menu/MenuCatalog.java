package com.companyleveltraining.backend.portal.menu;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

/**
 * 平台菜单与按钮权限目录（P0 静态实现）。
 * 角色编码沿用现有 admin / teacher / student（admin 在 P0 兼任超级管理员）。
 * 后续如拆分 super_admin 或接入 RBAC 表，可在此替换为数据库驱动。
 */
@Component
public class MenuCatalog {

    /** 菜单定义：code/title/icon/path/sort + 可见角色集合。 */
    private record MenuDef(String code, String title, String icon, String path, int sort, Set<String> roles) {
    }

    private static final List<MenuDef> MENUS = List.of(
        new MenuDef("portal_home",     "门户首页",     "home",      "/pages/portal-home/portal-home",                 1,  all()),
        new MenuDef("app_center",      "应用中心",     "apps",      "/pages/app-center/app-center",                   2,  all()),
        new MenuDef("lab_reservation", "实验室预约",   "calendar",  "/pages/student-reservation-apply/student-reservation-apply", 3, all()),
        new MenuDef("task_center",     "任务中心",     "task",      "/pages/task-center/task-center",                 4,  roles("teacher", "admin")),
        new MenuDef("notice",          "通知公告",     "notice",    "/pages/notice-list/notice-list",                 5,  all()),
        new MenuDef("news",            "校园资讯",     "news",      "/pages/news-list/news-list",                     6,  all()),
        new MenuDef("message_center",  "消息中心",     "message",   "/pages/message-center/message-center",           7,  all()),
        new MenuDef("calendar",        "我的日历",     "calendar",  "/pages/calendar/calendar",                       8,  all()),
        new MenuDef("ai_assistant",    "AI 校园助手",  "ai",        "/pages/ai-assistant/ai-assistant",               9,  all()),
        new MenuDef("repair_service",  "报修服务",     "tools",     "/pages/repair-service/repair-service",           10, all()),
        new MenuDef("personal_center", "个人中心",     "user",      "/pages/student-personal-info/student-personal-info", 11, all()),
        new MenuDef("admin_workbench", "管理员工作台", "dashboard", "/pages/admin-workbench/admin-workbench",          20, roles("admin")),
        new MenuDef("notice_manage",   "通知公告管理", "notice",    "/pages/admin-notice-manage/admin-notice-manage", 21, roles("admin")),
        new MenuDef("news_manage",     "校园资讯管理", "news",      "/pages/admin-news-manage/admin-news-manage",     22, roles("admin")),
        new MenuDef("app_manage",      "应用管理",     "apps",      "/pages/admin-app-manage/admin-app-manage",       23, roles("admin")),
        new MenuDef("lab_manage",      "实验室管理",   "flask",     "/pages/admin-lab-management/admin-lab-management", 24, roles("admin")),
        new MenuDef("repair_review",   "报修审核",     "tools",     "/pages/admin-repair-review/admin-repair-review", 25, roles("admin")),
        new MenuDef("user_manage",     "用户管理",     "user",      "/pages/admin-system-management/admin-system-management", 26, roles("admin")),
        new MenuDef("system",          "系统管理",     "setting",   "/pages/admin-system-management/admin-system-management", 27, roles("admin"))
    );

    /** 按钮权限：角色 -> 可见按钮编码集合。 */
    private static final List<MenuDef> ALL = MENUS;

    public List<MenuItem> menusForRole(String role) {
        return ALL.stream()
            .filter(m -> m.roles().contains(role))
            .sorted((a, b) -> Integer.compare(a.sort(), b.sort()))
            .map(m -> new MenuItem(m.code(), m.title(), m.icon(), m.path(), m.sort()))
            .collect(Collectors.toList());
    }

    public List<String> menuCodesForRole(String role) {
        return menusForRole(role).stream().map(MenuItem::code).collect(Collectors.toList());
    }

    /** 按钮级权限编码。 */
    public Set<String> buttonsForRole(String role) {
        Set<String> codes = new LinkedHashSet<>();
        // 所有登录用户都可提交预约、收藏应用
        codes.add("reservation:submit");
        codes.add("repair:submit");
        codes.add("app:favorite");
        if ("teacher".equals(role) || "admin".equals(role)) {
            codes.add("reservation:approve");
            codes.add("reservation:reject");
        }
        if ("admin".equals(role)) {
            codes.add("app:create");
            codes.add("app:edit");
            codes.add("app:delete");
            codes.add("notice:create");
            codes.add("notice:edit");
            codes.add("notice:delete");
            codes.add("notice:publish");
            codes.add("news:create");
            codes.add("news:edit");
            codes.add("news:delete");
            codes.add("news:publish");
            codes.add("user:create");
            codes.add("user:edit");
            codes.add("user:disable");
            codes.add("repair:approve");
            codes.add("repair:reject");
            codes.add("role:grant");
            codes.add("menu:grant");
        }
        return codes;
    }

    private static Set<String> all() {
        return roles("admin", "teacher", "student");
    }

    private static Set<String> roles(String... rs) {
        return new LinkedHashSet<>(List.of(rs));
    }
}
