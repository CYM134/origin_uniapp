package com.companyleveltraining.backend.ai.model;

import org.springframework.stereotype.Component;

import com.companyleveltraining.backend.ai.AiContextService;

@Component
public class AiPromptBuilder {

    public AiPrompt build(String question, AiContextService.AiContext context) {
        String systemPrompt = "你是实验室空间预约与协同管理系统的智能助手，请用简体中文简洁回答实验室空间预约、课表、通知公告等校园服务问题。"
            + "当用户询问实际数据（如我的预约、待办、未读消息、最新公告、近期日程、我的课表、实验室空余时间）时，"
            + "只能依据下面的【当前业务上下文】回答；上下文没有的信息要说明暂时无法确认，不要编造。"
            + "\n\n【当前业务上下文】\n" + context.prompt();
        return new AiPrompt(systemPrompt, question);
    }
}
