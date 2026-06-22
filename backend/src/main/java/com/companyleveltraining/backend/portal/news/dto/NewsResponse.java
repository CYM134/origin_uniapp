package com.companyleveltraining.backend.portal.news.dto;

/** 校园资讯响应对象。 */
public record NewsResponse(
    Long id,
    String newsNo,
    String title,
    String summary,
    String content,
    Long categoryId,
    String categoryCode,
    String categoryName,
    String coverImage,
    String author,
    String status,
    boolean top,
    String publishTime,
    Long publisherId,
    Long viewCount,
    String createTime
) {
}
