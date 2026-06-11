package com.companyleveltraining.backend.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AuthAsyncService {

    private static final Logger log = LoggerFactory.getLogger(AuthAsyncService.class);

    private final UserAccountRepository userAccountRepository;

    public AuthAsyncService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Async
    public void updateLoginSuccessAsync(Long userId) {
        try {
            userAccountRepository.updateLoginSuccess(userId);
        } catch (Exception ex) {
            log.warn("Async updateLoginSuccess failed for userId={}", userId, ex);
        }
    }
}
