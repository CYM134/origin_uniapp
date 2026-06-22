package com.companyleveltraining.backend.portal.news.dto;

import jakarta.validation.constraints.NotBlank;

/** 新增 / 编辑资讯请求体。 */
public record NewsSaveRequest(
    @NotBlank(message = "标题不能为空") String title,
    String summary,
    String content,
    Long categoryId,
    String coverImage,
    String author,
    Boolean top,
    String status
) {
}
