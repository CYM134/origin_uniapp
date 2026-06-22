package com.companyleveltraining.backend.ai.dto;

import jakarta.validation.constraints.NotBlank;

/** AI 对话请求体。conversationId 为空表示新建会话。 */
public record AiChatRequest(
    @NotBlank(message = "问题内容不能为空") String message,
    Long conversationId
) {
}
