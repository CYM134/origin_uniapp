package com.companyleveltraining.backend.profile;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.companyleveltraining.backend.common.SecurityUtils;
import com.companyleveltraining.backend.profile.dto.ProfileUpdateRequest;
import com.companyleveltraining.backend.security.SecurityUser;

/**
 * 学生/教师个人资料接口，对应 student-personal-info / teacher-personal-info 页。
 */
@RestController
@RequestMapping("/api")
public class ProfileController {

    private final ProfileService service;

    public ProfileController(ProfileService service) {
        this.service = service;
    }

    @GetMapping("/student/profile")
    public Map<String, Object> getStudentProfile() {
        return service.getStudentProfile(requireRole("student").id());
    }

    @PutMapping("/student/profile")
    public Map<String, Object> updateStudentProfile(@RequestBody ProfileUpdateRequest req) {
        return service.updateStudentProfile(requireRole("student").id(), req);
    }

    @PostMapping("/student/avatar")
    public Map<String, Object> updateStudentAvatar(@RequestBody Map<String, String> body) {
        SecurityUser user = requireRole("student");
        service.updateAvatar(user.id(), body.get("avatar"));
        return Map.of("success", true, "avatar", body.getOrDefault("avatar", ""));
    }

    @GetMapping("/teacher/profile")
    public Map<String, Object> getTeacherProfile() {
        return service.getTeacherProfile(requireRole("teacher").id());
    }

    @PutMapping("/teacher/profile")
    public Map<String, Object> updateTeacherProfile(@RequestBody ProfileUpdateRequest req) {
        return service.updateTeacherProfile(requireRole("teacher").id(), req);
    }

    @PostMapping("/teacher/avatar")
    public Map<String, Object> updateTeacherAvatar(@RequestBody Map<String, String> body) {
        SecurityUser user = requireRole("teacher");
        service.updateAvatar(user.id(), body.get("avatar"));
        return Map.of("success", true, "avatar", body.getOrDefault("avatar", ""));
    }

    private SecurityUser requireRole(String role) {
        SecurityUtils.requireRole(role);
        return SecurityUtils.currentUser();
    }
}
