package com.companyleveltraining.backend.portal.calendar.dto;

/** 日历事件响应对象。startTime/endTime 为格式化字符串。 */
public record CalendarEventResponse(
    Long id,
    String title,
    String eventType,
    String startTime,
    String endTime,
    String location,
    String remark,
    String relatedBusinessType,
    Long relatedBusinessId,
    boolean editable
) {
}
