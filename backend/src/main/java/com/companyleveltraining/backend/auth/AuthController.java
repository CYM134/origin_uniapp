package com.companyleveltraining.backend.auth;

import java.util.Map;

import com.companyleveltraining.backend.auth.dto.ChangePasswordRequest;
import com.companyleveltraining.backend.auth.dto.LoginRequest;
import com.companyleveltraining.backend.auth.dto.LoginResponse;
import com.companyleveltraining.backend.auth.dto.StudentRegisterRequest;
import com.companyleveltraining.backend.auth.dto.TeacherRegisterRequest;
import com.companyleveltraining.backend.auth.dto.UserProfileResponse;
import com.companyleveltraining.backend.common.SecurityUtils;
import com.companyleveltraining.backend.security.SecurityUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final RegistrationService registrationService;

    public AuthController(AuthService authService, RegistrationService registrationService) {
        this.authService = authService;
        this.registrationService = registrationService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserProfileResponse currentUser() {
        return authService.currentUser();
    }

    @PostMapping("/student/register")
    public Map<String, Object> registerStudent(@Valid @RequestBody StudentRegisterRequest request) {
        Long userId = registrationService.registerStudent(request);
        return Map.of("success", true, "userId", userId, "message", "注册成功，请登录");
    }

    @PostMapping("/teacher/register")
    public Map<String, Object> registerTeacher(@Valid @RequestBody TeacherRegisterRequest request) {
        Long applicationId = registrationService.registerTeacher(request);
        return Map.of("success", true, "applicationId", applicationId, "message", "注册申请已提交，请等待管理员审核");
    }

    @GetMapping("/student/exists")
    public Map<String, Object> studentExists(@RequestParam String studentNo) {
        return Map.of("exists", registrationService.studentExists(studentNo));
    }

    @GetMapping("/teacher/exists")
    public Map<String, Object> teacherExists(@RequestParam String teacherNo) {
        return Map.of("exists", registrationService.teacherExists(teacherNo));
    }

    @PostMapping("/change-password")
    public Map<String, Object> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        SecurityUser user = SecurityUtils.currentUser();
        registrationService.changePassword(user.id(), user.role(), request);
        return Map.of("success", true, "message", "密码修改成功");
    }

    @PostMapping("/logout")
    public Map<String, Object> logout() {
        SecurityUser user = SecurityUtils.currentUser();
        registrationService.logout(user);
        return Map.of("success", true, "message", "已退出登录");
    }
}
