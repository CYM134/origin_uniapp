package com.companyleveltraining.backend.auth;

import com.companyleveltraining.backend.auth.dto.LoginRequest;
import com.companyleveltraining.backend.auth.dto.LoginResponse;
import com.companyleveltraining.backend.auth.dto.UserProfileResponse;
import com.companyleveltraining.backend.security.JwtService;
import com.companyleveltraining.backend.security.SecurityUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserAccountRepository userAccountRepository;
    private final AuthAsyncService authAsyncService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
        UserAccountRepository userAccountRepository,
        AuthAsyncService authAsyncService,
        PasswordEncoder passwordEncoder,
        JwtService jwtService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.authAsyncService = authAsyncService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        UserAccount user = userAccountRepository.findActiveByRoleAndAccountNo(
                request.role(),
                request.accountNo()
            )
            .orElseThrow(() -> new BadCredentialsException("账号或密码错误"));

        if (!passwordMatches(request.password(), user.passwordHash())) {
            throw new BadCredentialsException("账号或密码错误");
        }

        authAsyncService.updateLoginSuccessAsync(user.id());
        String token = jwtService.generateToken(user);

        return new LoginResponse(
            token,
            jwtService.getExpirationSeconds(),
            toUserProfileResponse(user)
        );
    }

    public UserProfileResponse currentUser() {
        SecurityUser principal = (SecurityUser) SecurityContextHolder.getContext()
            .getAuthentication()
            .getPrincipal();

        if (hasCompleteProfile(principal)) {
            log.info(
                "Resolved current user profile from JWT: id={}, role={}, accountNo={}, college={}, major={}, department={}",
                principal.id(),
                principal.role(),
                principal.accountNo(),
                principal.college(),
                principal.major(),
                principal.department()
            );

            return new UserProfileResponse(
                principal.id(),
                principal.accountNo(),
                principal.role(),
                principal.realName(),
                principal.gender(),
                principal.phone(),
                principal.email(),
                principal.status(),
                principal.college(),
                principal.major(),
                principal.department(),
                principal.positionTitle()
            );
        }

        UserAccount user = userAccountRepository.findProfileById(principal.id())
            .orElseThrow(() -> new BadCredentialsException("用户不存在或已失效"));

        log.info(
            "Resolved current user profile from DB fallback: id={}, role={}, accountNo={}, college={}, major={}, department={}",
            user.id(),
            user.role(),
            user.accountNo(),
            user.college(),
            user.major(),
            user.department()
        );

        return toUserProfileResponse(user);
    }

    private boolean passwordMatches(String rawPassword, String storedPassword) {
        if (storedPassword == null || storedPassword.isBlank()) {
            return false;
        }
        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return storedPassword.equals(rawPassword);
    }

    private UserProfileResponse toUserProfileResponse(UserAccount user) {
        return new UserProfileResponse(
            user.id(),
            user.accountNo(),
            user.role(),
            user.realName(),
            user.gender(),
            user.phone(),
            user.email(),
            user.status(),
            user.college(),
            user.major(),
            user.department(),
            user.positionTitle()
        );
    }

    private boolean hasCompleteProfile(SecurityUser principal) {
        if ("student".equals(principal.role())) {
            return principal.college() != null && principal.major() != null;
        }
        if ("teacher".equals(principal.role())) {
            return principal.college() != null && principal.department() != null;
        }
        return true;
    }
}
