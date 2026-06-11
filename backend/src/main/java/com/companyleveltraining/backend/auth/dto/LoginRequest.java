package com.companyleveltraining.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
    @NotBlank(message = "账号不能为空")
    String accountNo,
    @NotBlank(message = "密码不能为空")
    String password,
    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "admin|teacher|student", message = "角色必须是 admin、teacher 或 student")
    String role
) {
}

