package com.companyleveltraining.backend.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 教师发起预约申请请求，对应 teacher-reservation-apply 表单。
 */
public record TeacherReservationRequest(
    @NotNull(message = "请选择实验室") Long labId,
    @NotBlank(message = "请选择预约日期") String date,
    @NotBlank(message = "请选择开始时间") String startTime,
    @NotBlank(message = "请选择结束时间") String endTime,
    Integer studentCount,
    @NotBlank(message = "请填写课程名称") String courseName,
    String courseType,
    String remark
) {
}
