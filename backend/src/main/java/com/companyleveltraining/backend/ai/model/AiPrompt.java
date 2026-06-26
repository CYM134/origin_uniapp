package com.companyleveltraining.backend.ai.model;

public record AiPrompt(
    String systemPrompt,
    String userMessage
) {
}
