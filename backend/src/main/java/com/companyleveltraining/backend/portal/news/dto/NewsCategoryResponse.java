package com.companyleveltraining.backend.portal.news.dto;

/** 资讯分类响应对象。 */
public record NewsCategoryResponse(
    Long id,
    String categoryName,
    String categoryCode,
    Integer sortOrder,
    Integer status
) {
}
