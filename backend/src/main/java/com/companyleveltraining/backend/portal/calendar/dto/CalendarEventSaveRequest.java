package com.companyleveltraining.backend.portal.calendar.dto;

import jakarta.validation.constraints.NotBlank;

/** 新增 / 编辑个人日历事件请求体。时间格式 yyyy-MM-dd HH:mm:ss。 */
public record CalendarEventSaveRequest(
    @NotBlank(message = "标题不能为空") String title,
    String eventType,
    @NotBlank(message = "开始时间不能为空") String startTime,
    String endTime,
    String location,
    String remark
) {
}
