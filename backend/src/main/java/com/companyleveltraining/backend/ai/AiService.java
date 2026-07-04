package com.companyleveltraining.backend.ai;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.companyleveltraining.backend.ai.model.AiModelClient;
import com.companyleveltraining.backend.ai.model.AiPromptBuilder;
import com.companyleveltraining.backend.ai.dto.AiChatRequest;
import com.companyleveltraining.backend.common.BusinessException;
import com.companyleveltraining.backend.security.SecurityUser;

/**
 * AI 校园助手业务。
 * 优先调用配置的大模型接口（OpenAI 兼容 chat/completions）；未配置或调用失败时，
 * 自动降级为内置的规则问答，保证入口可用且不暴露/硬编码密钥。
 */
@Service
public class AiService {

    private final AiRepository repository;
    private final AiContextService contextService;
    private final AiModelClient modelClient;
    private final AiPromptBuilder promptBuilder;

    public AiService(AiRepository repository, AiContextService contextService,
                     AiModelClient modelClient, AiPromptBuilder promptBuilder) {
        this.repository = repository;
        this.contextService = contextService;
        this.modelClient = modelClient;
        this.promptBuilder = promptBuilder;
    }

    public Map<String, Object> chat(SecurityUser user, AiChatRequest req) {
        Long conversationId = req.conversationId();
        if (conversationId == null) {
            conversationId = repository.createConversation(user.id(), summarize(req.message()));
        } else if (repository.findOwnedConversation(conversationId, user.id()).isEmpty()) {
            throw BusinessException.notFound("会话不存在");
        }
        repository.addMessage(conversationId, "user", req.message());

        AiContextService.AiContext context = contextService.build(user);
        String source = "fallback";
        String reply;
        String directReply = contextualReply(req.message(), context);
        if (directReply != null) {
            reply = directReply;
            source = "context";
        } else if (modelClient.available()) {
            var modelReply = modelClient.chat(promptBuilder.build(req.message(), context));
            if (modelReply.isPresent()) {
                reply = modelReply.get();
                source = "model";
            } else {
                reply = ruleBasedReply(req.message(), context);
            }
        } else {
            reply = ruleBasedReply(req.message(), context);
        }

        repository.addMessage(conversationId, "assistant", reply);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("conversationId", conversationId);
        result.put("reply", reply);
        result.put("source", source);
        result.put("modelProvider", "model".equals(source) ? modelClient.provider() : "fallback");
        result.put("contextIncluded", true);
        return result;
    }

    public List<Map<String, Object>> conversations(Long userId) {
        return repository.listConversations(userId);
    }

    public Map<String, Object> conversationDetail(Long conversationId, Long userId) {
        if (repository.findOwnedConversation(conversationId, userId).isEmpty()) {
            throw BusinessException.notFound("会话不存在");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("conversationId", conversationId);
        result.put("messages", repository.listMessages(conversationId));
        return result;
    }

    /** 内置规则问答：覆盖平台常见问题，保证未接入模型时仍可用。 */
    private String ruleBasedReply(String q, AiContextService.AiContext context) {
        String contextual = contextualReply(q, context);
        if (contextual != null) {
            return contextual;
        }

        String s = q == null ? "" : q.toLowerCase();
        if (contains(q, "预约") && (contains(q, "怎么") || contains(q, "如何") || contains(q, "流程"))) {
            return "实验室预约流程：在「实验室预约」中选择可用实验室与时间段，填写用途与人数后提交。"
                + "学生申请先由教师初审、再由管理员终审；教师申请由管理员直接审批。"
                + "审批结果会在「消息中心」通知您。";
        }
        if (contains(q, "冲突") || (contains(q, "预约") && contains(q, "规则"))) {
            return "预约规则：同一实验室同一时间段不可重复预约；开始时间必须早于结束时间；"
                + "停用或维修中的实验室不可预约；取消或被驳回的预约不占用时间段。";
        }
        if (contains(q, "审批") || contains(q, "待办")) {
            return "审批相关：教师/管理员可在「任务中心」查看待办、已办与我发起的事项，"
                + "对预约进行通过或驳回，并填写审批意见。审批结果会即时推送消息给申请人。";
        }
        if (contains(q, "应用") || contains(q, "入口")) {
            return "「应用中心」汇总了校园常用服务入口，可按分类筛选、搜索并收藏常用应用，"
                + "收藏后会出现在门户首页的「我的应用」中。";
        }
        if (contains(q, "通知") || contains(q, "公告") || contains(q, "资讯")) {
            return "「通知公告」展示学校与平台发布的公告，「校园资讯」展示校园新闻与动态，"
                + "门户首页也会展示最新通知与资讯，点击即可查看详情。";
        }
        if (contains(q, "消息") || contains(q, "未读")) {
            return "「消息中心」集中展示系统通知、预约结果与审批提醒，右上角角标显示未读数量，"
                + "可单条标记已读或一键全部已读。";
        }
        if (contains(q, "你好") || contains(q, "hello") || s.contains("hi")) {
            return "有什么我可以帮助你的？";
        }
        return "我可以帮您了解实验室空间预约与协同管理系统的功能，例如：实验室预约与审批流程、预约规则、"
            + "应用中心与收藏、通知公告与消息中心等。请换一种方式描述您的问题，我会尽力解答。";
    }

    private String contextualReply(String q, AiContextService.AiContext context) {
        if (q == null || q.isBlank()) {
            return null;
        }
        boolean asksActualData = containsAny(q, "我", "我的", "多少", "几个", "有没有", "当前", "现在", "今天",
            "明天", "最近", "最新", "未读", "待处理", "待审核", "待审批", "待办", "状态");

        if (containsAny(q, "课表", "课程安排", "上课安排", "授课安排")) {
            return context.scheduleSummary();
        }
        if (containsAny(q, "空余", "空闲", "可用时间", "可预约时间", "空余时间段", "课余时间", "时间段")
            && containsAny(q, "实验室", "教室", "实验间")) {
            return context.availabilitySummary();
        }
        if (containsAny(q, "消息", "未读", "通知提醒") && asksActualData) {
            return context.messageSummary();
        }
        if (containsAny(q, "日程", "日历", "安排", "今天", "明天") && asksActualData) {
            return context.calendarSummary();
        }
        if (containsAny(q, "公告", "通知公告", "资讯", "新闻") && asksActualData) {
            return context.noticeSummary();
        }
        if (containsAny(q, "待办", "待处理", "审批", "审核", "注册审核", "未审批", "未审核") && asksActualData) {
            return context.taskSummary();
        }
        if (contains(q, "预约") && asksActualData) {
            return context.reservationSummary();
        }
        if (containsAny(q, "我的数据", "当前数据", "实际情况", "概况", "汇总")) {
            return String.join("\n", context.reservationSummary(), context.taskSummary(),
                context.messageSummary(), context.calendarSummary(), context.scheduleSummary(),
                context.availabilitySummary(), context.noticeSummary());
        }
        return null;
    }

    private boolean contains(String text, String kw) {
        return text != null && text.contains(kw);
    }

    private boolean containsAny(String text, String... keywords) {
        if (text == null) {
            return false;
        }
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String summarize(String message) {
        if (message == null || message.isBlank()) {
            return "新会话";
        }
        String trimmed = message.strip();
        return trimmed.length() > 20 ? trimmed.substring(0, 20) : trimmed;
    }
}
