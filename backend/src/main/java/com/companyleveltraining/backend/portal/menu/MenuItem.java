package com.companyleveltraining.backend.portal.menu;

/** 菜单项响应对象。 */
public record MenuItem(
    String code,
    String title,
    String icon,
    String path,
    Integer sortOrder
) {
}
