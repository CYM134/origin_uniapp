package com.companyleveltraining.backend.common.ratelimit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RateLimitProperties {

    private final boolean enabled;
    private final int loginPerMinute;
    private final int registerPerFiveMinutes;
    private final int aiPerMinute;

    public RateLimitProperties(
        @Value("${rate-limit.enabled:true}") boolean enabled,
        @Value("${rate-limit.login-per-minute:10}") int loginPerMinute,
        @Value("${rate-limit.register-per-five-minutes:5}") int registerPerFiveMinutes,
        @Value("${rate-limit.ai-per-minute:30}") int aiPerMinute
    ) {
        this.enabled = enabled;
        this.loginPerMinute = loginPerMinute;
        this.registerPerFiveMinutes = registerPerFiveMinutes;
        this.aiPerMinute = aiPerMinute;
    }

    public boolean enabled() {
        return enabled;
    }

    public int loginPerMinute() {
        return loginPerMinute;
    }

    public int registerPerFiveMinutes() {
        return registerPerFiveMinutes;
    }

    public int aiPerMinute() {
        return aiPerMinute;
    }
}
