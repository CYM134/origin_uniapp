package com.companyleveltraining.backend.auth.dto;

public record UserProfileResponse(
    Long id,
    String accountNo,
    String role,
    String realName,
    String gender,
    String phone,
    String email,
    String status,
    String college,
    String major,
    String department,
    String positionTitle
) {
}
