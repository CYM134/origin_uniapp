package com.companyleveltraining.backend.repair.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 师生提交报修申请。 */
public record RepairCreateRequest(
    @NotNull(message = "请选择报修实验室") Long labId,
    @NotBlank(message = "请选择故障类型") String faultType,
    String urgency,
    @NotBlank(message = "请输入报修标题") @Size(max = 150, message = "标题不能超过150字") String title,
    @NotBlank(message = "请输入故障描述") String description,
    String contactName,
    String contactPhone,
    String preferredTime
) {
}
