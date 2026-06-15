package com.companyleveltraining.backend.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.companyleveltraining.backend.audit.AuditLogService;
import com.companyleveltraining.backend.auth.dto.ChangePasswordRequest;
import com.companyleveltraining.backend.auth.dto.StudentRegisterRequest;
import com.companyleveltraining.backend.auth.dto.TeacherRegisterRequest;
import com.companyleveltraining.backend.common.BizNoGenerator;
import com.companyleveltraining.backend.common.BusinessException;

/**
 * 注册、改密、退出等账号相关业务。
 */
@Service
public class RegistrationService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final BizNoGenerator bizNoGenerator;
    private final AuditLogService auditLogService;

    public RegistrationService(AccountRepository accountRepository, PasswordEncoder passwordEncoder,
                               BizNoGenerator bizNoGenerator, AuditLogService auditLogService) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.bizNoGenerator = bizNoGenerator;
        this.auditLogService = auditLogService;
    }

    public boolean studentExists(String studentNo) {
        return accountRepository.existsAccount("student", studentNo);
    }

    public boolean teacherExists(String teacherNo) {
        return accountRepository.existsAccount("teacher", teacherNo)
            || accountRepository.existsPendingTeacherApplication(teacherNo);
    }

    @Transactional
    public Long registerStudent(StudentRegisterRequest req) {
        if (req.password().length() < 6) {
            throw BusinessException.badRequest("密码长度不能少于 6 位");
        }
        if (accountRepository.existsAccount("student", req.studentId())) {
            throw BusinessException.conflict("该学号已注册");
        }
        String hash = passwordEncoder.encode(req.password());
        Long userId = accountRepository.insertStudentUser(req, hash);
        accountRepository.insertStudentProfile(userId, req);
        auditLogService.record(userId, "student", "auth", "register", "sys_users", userId, null);
        return userId;
    }

    public Long registerTeacher(TeacherRegisterRequest req) {
        if (req.password().length() < 6) {
            throw BusinessException.badRequest("密码长度不能少于 6 位");
        }
        if (teacherExists(req.teacherId())) {
            throw BusinessException.conflict("该工号已注册或已提交待审核申请");
        }
        String hash = passwordEncoder.encode(req.password());
        String applicationNo = bizNoGenerator.generate("TRA");
        Long appId = accountRepository.insertTeacherRegistration(req, applicationNo, hash);
        auditLogService.record(null, "anonymous", "auth", "teacher_register_apply",
            "teacher_registration_applications", appId, null);
        return appId;
    }

    public void changePassword(Long userId, String role, ChangePasswordRequest req) {
        if (req.newPassword().length() < 6) {
            throw BusinessException.badRequest("新密码长度不能少于 6 位");
        }
        String storedHash = accountRepository.findPasswordHashById(userId)
            .orElseThrow(() -> BusinessException.notFound("用户不存在"));
        if (!passwordMatches(req.oldPassword(), storedHash)) {
            throw BusinessException.badRequest("原密码不正确");
        }
        accountRepository.updatePassword(userId, passwordEncoder.encode(req.newPassword()));
        auditLogService.record(userId, role, "auth", "change_password", "sys_users", userId, null);
    }

    public void logout(Long userId, String role) {
        auditLogService.record(userId, role, "auth", "logout", "sys_users", userId, null);
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
}
