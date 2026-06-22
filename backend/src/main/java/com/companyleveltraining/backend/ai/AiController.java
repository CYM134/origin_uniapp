package com.companyleveltraining.backend.ai;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.companyleveltraining.backend.ai.dto.AiChatRequest;
import com.companyleveltraining.backend.common.SecurityUtils;
import com.companyleveltraining.backend.security.SecurityUser;

/** AI 校园助手接口：发送问题、会话列表、会话详情。 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService service;

    public AiController(AiService service) {
        this.service = service;
    }

    @PostMapping("/chat")
    public Map<String, Object> chat(@Valid @RequestBody AiChatRequest req) {
        SecurityUser user = SecurityUtils.currentUser();
        return service.chat(user, req);
    }

    @GetMapping("/conversations")
    public List<Map<String, Object>> conversations() {
        return service.conversations(SecurityUtils.currentUserId());
    }

    @GetMapping("/conversations/{id}")
    public Map<String, Object> conversationDetail(@PathVariable Long id) {
        return service.conversationDetail(id, SecurityUtils.currentUserId());
    }
}
