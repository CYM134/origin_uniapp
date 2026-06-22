package com.companyleveltraining.backend.portal.app.dto;

import jakarta.validation.constraints.NotBlank;

/** 新增 / 编辑应用请求体（管理员应用管理）。 */
public record AppSaveRequest(
    @NotBlank(message = "应用名称不能为空") String appName,
    @NotBlank(message = "应用编码不能为空") String appCode,
    Long categoryId,
    String icon,
    String description,
    String routePath,
    String externalUrl,
    String openType,
    String visibleRoles,
    Boolean hot,
    Boolean latest,
    Integer status,
    Integer sortOrder
) {
}
