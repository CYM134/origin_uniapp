package com.companyleveltraining.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 修改密码请求，对应学生/教师个人信息页的修改密码弹窗。
 */
public record ChangePasswordRequest(
    @NotBlank(message = "原密码不能为空") String oldPassword,
    @NotBlank(message = "新密码不能为空") String newPassword
) {
}
