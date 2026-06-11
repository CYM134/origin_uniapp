package com.companyleveltraining.backend.auth;

public record UserAccount(
    Long id,
    String accountNo,
    String role,
    String passwordHash,
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
