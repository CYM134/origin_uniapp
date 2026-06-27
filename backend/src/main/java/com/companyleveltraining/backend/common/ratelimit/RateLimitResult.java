package com.companyleveltraining.backend.common.ratelimit;

public record RateLimitResult(boolean allowed, long limit, long remaining, long retryAfterSeconds) {
}
