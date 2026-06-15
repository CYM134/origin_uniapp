package com.companyleveltraining.backend.profile.dto;

/**
 * 学生/教师个人资料更新请求。各端使用其相关字段：
 * 学生用 name/gender/phone/email/college/major；教师用 name/gender/phone/email/college/department/positionTitle。
 */
public record ProfileUpdateRequest(
    String name,
    String gender,
    String phone,
    String email,
    String college,
    String major,
    String department,
    String positionTitle
) {
}
