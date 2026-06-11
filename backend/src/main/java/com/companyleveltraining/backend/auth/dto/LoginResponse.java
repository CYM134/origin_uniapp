package com.companyleveltraining.backend.auth.dto;

public record LoginResponse(
    String accessToken,
    long expiresIn,
    UserProfileResponse user
) {
}
