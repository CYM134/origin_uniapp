package com.companyleveltraining.backend.portal.app.dto;

/**
 * 应用响应对象。字段对齐前端应用宫格与门户首页使用：
 * - hot/latest/favorite 为布尔标识，分别表示热门、最新、是否已收藏。
 */
public record AppResponse(
    Long id,
    String appName,
    String appCode,
    Long categoryId,
    String categoryName,
    String icon,
    String description,
    String routePath,
    String externalUrl,
    String openType,
    String visibleRoles,
    boolean hot,
    boolean latest,
    boolean favorite,
    Integer status,
    Integer sortOrder,
    Long visitCount
) {
}
