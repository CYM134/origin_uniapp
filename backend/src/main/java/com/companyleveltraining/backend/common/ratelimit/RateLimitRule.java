package com.companyleveltraining.backend.common.ratelimit;

import java.time.Duration;

record RateLimitRule(String id, String method, String pathPattern, int limit, Duration window, Subject subject) {

    enum Subject {
        IP,
        USER_OR_IP
    }
}
