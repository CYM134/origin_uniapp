package com.companyleveltraining.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 学生自助注册请求，对应 student-register 页表单。
 */
public record StudentRegisterRequest(
    @NotBlank(message = "学号不能为空") String studentId,
    @NotBlank(message = "姓名不能为空") String name,
    String gender,
    @NotBlank(message = "学院不能为空") String college,
    @NotBlank(message = "年级专业不能为空") String major,
    String phone,
    @NotBlank(message = "密码不能为空") String password
) {
}
