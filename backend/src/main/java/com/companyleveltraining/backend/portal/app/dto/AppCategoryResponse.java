package com.companyleveltraining.backend.portal.app.dto;

/** 应用分类响应对象。 */
public record AppCategoryResponse(
    Long id,
    String categoryName,
    String categoryCode,
    String icon,
    Integer sortOrder,
    Integer status
) {
}
