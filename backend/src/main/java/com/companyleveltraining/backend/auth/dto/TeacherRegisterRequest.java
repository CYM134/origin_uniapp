package com.companyleveltraining.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 教师自助注册请求，对应 teacher-register 页表单。提交后生成一条待审核的教师注册申请，
 * 由管理员在 admin-teacher-registration 页审核通过后才会创建正式教师账号。
 */
public record TeacherRegisterRequest(
    @NotBlank(message = "工号不能为空") String teacherId,
    @NotBlank(message = "姓名不能为空") String name,
    String gender,
    @NotBlank(message = "学院不能为空") String college,
    String department,
    String positionTitle,
    @NotBlank(message = "手机号不能为空") String phone,
    String email,
    @NotBlank(message = "密码不能为空") String password
) {
}
