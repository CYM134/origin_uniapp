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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证与账号", description = "登录、当前用户、注册、改密和退出登录接口")
public class AuthController {

    private final AuthService authService;
    private final RegistrationService registrationService;

    public AuthController(AuthService authService, RegistrationService registrationService) {
        this.authService = authService;
        this.registrationService = registrationService;
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "按角色、账号和密码登录，返回 JWT 访问令牌及用户基础信息")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @Operation(summary = "当前登录用户", description = "根据请求中的 JWT 返回当前用户资料")
    public UserProfileResponse currentUser() {
        return authService.currentUser();
    }

    @PostMapping("/student/register")
    @Operation(summary = "学生注册", description = "创建学生账号，注册成功后可直接登录")
    public Map<String, Object> registerStudent(@Valid @RequestBody StudentRegisterRequest request) {
        Long userId = registrationService.registerStudent(request);
        return Map.of("success", true, "userId", userId, "message", "注册成功，请登录");
    }

    @PostMapping("/teacher/register")
    @Operation(summary = "教师注册申请", description = "提交教师注册申请，等待管理员审核后生效")
    public Map<String, Object> registerTeacher(@Valid @RequestBody TeacherRegisterRequest request) {
        Long applicationId = registrationService.registerTeacher(request);
        return Map.of("success", true, "applicationId", applicationId, "message", "注册申请已提交，请等待管理员审核");
    }

    @GetMapping("/student/exists")
    @Operation(summary = "检查学生学号", description = "检查学生学号是否已存在")
    public Map<String, Object> studentExists(@RequestParam String studentNo) {
        return Map.of("exists", registrationService.studentExists(studentNo));
    }

    @GetMapping("/teacher/exists")
    @Operation(summary = "检查教师工号", description = "检查教师工号是否已存在")
    public Map<String, Object> teacherExists(@RequestParam String teacherNo) {
        return Map.of("exists", registrationService.teacherExists(teacherNo));
    }

    @PostMapping("/change-password")
    @Operation(summary = "修改密码", description = "当前登录用户修改自己的密码，修改后旧会话会失效")
    public Map<String, Object> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        SecurityUser user = SecurityUtils.currentUser();
        registrationService.changePassword(user.id(), user.role(), request);
        return Map.of("success", true, "message", "密码修改成功");
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录", description = "注销当前登录会话，Redis 会话校验开启时令当前 token 失效")
    public Map<String, Object> logout() {
        SecurityUser user = SecurityUtils.currentUser();
        registrationService.logout(user);
        return Map.of("success", true, "message", "已退出登录");
    }
}
