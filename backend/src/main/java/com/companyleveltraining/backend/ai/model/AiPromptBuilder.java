package com.companyleveltraining.backend.ai.model;

import org.springframework.stereotype.Component;

import com.companyleveltraining.backend.ai.AiContextService;

@Component
public class AiPromptBuilder {

    public AiPrompt build(String question, AiContextService.AiContext context) {
        String systemPrompt = "你是校园综合服务平台的智能助手，请用简体中文简洁回答校园服务、实验室预约、通知公告等问题。"
            + "当用户询问实际数据（如我的预约、待办、未读消息、最新公告、近期日程）时，"
            + "只能依据下面的【当前业务上下文】回答；上下文没有的信息要说明暂时无法确认，不要编造。"
            + "\n\n【当前业务上下文】\n" + context.prompt();
        return new AiPrompt(systemPrompt, question);
    }
}
