package com.companyleveltraining.backend.portal.notice.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

/** 新增 / 编辑通知公告请求体。 */
public record NoticeSaveRequest(
    @NotBlank(message = "标题不能为空") String title,
    @NotBlank(message = "正文不能为空") String content,
    String noticeType,
    String publishScope,
    List<String> targetRoles,
    Boolean top,
    String status
) {
}
