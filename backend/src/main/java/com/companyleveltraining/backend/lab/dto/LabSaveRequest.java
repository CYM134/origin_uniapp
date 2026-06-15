package com.companyleveltraining.backend.lab.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 新增 / 编辑实验室请求体。对应 admin-lab-management 的 currentLab 表单。
 */
public record LabSaveRequest(
    @NotBlank(message = "实验室名称不能为空") String name,
    @NotBlank(message = "实验室位置不能为空") String location,
    String equipment,
    String image,
    Integer capacity,
    String status
) {
}
