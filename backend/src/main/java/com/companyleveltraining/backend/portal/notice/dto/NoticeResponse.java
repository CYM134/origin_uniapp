package com.companyleveltraining.backend.portal.notice.dto;

/** 通知公告响应对象。read 表示当前用户是否已读（管理端列表恒为 false）。 */
public record NoticeResponse(
    Long id,
    String noticeNo,
    String title,
    String content,
    String noticeType,
    String publishScope,
    String targetRoles,
    boolean top,
    String status,
    String publishTime,
    Long publisherId,
    String publisherName,
    Long viewCount,
    String createTime,
    boolean read
) {
}
