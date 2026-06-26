package com.companyleveltraining.backend.ai;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "AI 问答", description = "校园 AI 助手会话、上下文问答和历史记录接口")
public class AiController {

    private final AiService service;

    public AiController(AiService service) {
        this.service = service;
    }

    @PostMapping("/chat")
    @Operation(summary = "发送 AI 问题", description = "按当前用户角色查询业务上下文后生成回答，并保存会话记录")
    public Map<String, Object> chat(@Valid @RequestBody AiChatRequest req) {
        SecurityUser user = SecurityUtils.currentUser();
        return service.chat(user, req);
    }

    @GetMapping("/conversations")
    @Operation(summary = "AI 会话列表", description = "查询当前用户的 AI 问答会话列表")
    public List<Map<String, Object>> conversations() {
        return service.conversations(SecurityUtils.currentUserId());
    }

    @GetMapping("/conversations/{id}")
    @Operation(summary = "AI 会话详情", description = "查询当前用户指定 AI 会话的消息明细")
    public Map<String, Object> conversationDetail(@PathVariable Long id) {
        return service.conversationDetail(id, SecurityUtils.currentUserId());
    }
}
