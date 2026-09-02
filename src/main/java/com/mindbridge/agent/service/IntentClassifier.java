package com.mindbridge.agent.service;

import com.mindbridge.agent.domain.IntentType;
import com.mindbridge.agent.service.ai.AiClient;
import com.mindbridge.agent.service.ai.AiMessage;
import com.mindbridge.agent.service.ai.PromptTemplates;
import com.mindbridge.agent.service.ai.RiskLexicon;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
/**
 * 用户意图分类服务。
 *
 * <p>把每轮输入路由到普通聊天、心理咨询或高风险处理链路。</p>
 */
public class IntentClassifier {

    private static final List<String> GENERAL_TASK_WORDS = List.of(
            "java", "python", "javascript", "代码", "编程", "程序", "算法", "数据库", "spring", "maven",
            "前端", "后端", "项目", "接口", "bug", "报错", "作业", "论文", "翻译", "总结", "解释",
            "怎么写", "如何", "是什么", "为什么", "给我", "帮我", "推荐", "查询", "天气", "路线"
    );

    private final AiClient aiClient;

    public IntentClassifier(AiClient aiClient) {
        this.aiClient = aiClient;
    }

    public IntentType classify(String input) {
        return classify(input, List.of());
    }

    public IntentType classify(String input, List<AiMessage> history) {
        String normalized = input.toLowerCase(Locale.ROOT);
        // 高风险表达优先级最高，不交给普通任务规则覆盖。
        if (RiskLexicon.hasHighRiskSignal(normalized)) {
            return IntentType.RISK;
        }
        // 学习、编程、作业等明确普通任务直接走 CHAT，避免误触发后台评估。
        if (isClearlyGeneralTask(normalized)) {
            return IntentType.CHAT;
        }
        try {
            String label = aiClient.complete(PromptTemplates.intentPrompt(history, input)).trim().toUpperCase();
            if (label.contains("RISK")) {
                return IntentType.RISK;
            }
            if (label.contains("CONSULT")) {
                return IntentType.CONSULT;
            }
            if (label.contains("CHAT")) {
                return IntentType.CHAT;
            }
        } catch (Exception ignored) {
            // Keyword fallback keeps the route deterministic when the model is unavailable.
        }
        if (RiskLexicon.hasConsultSignal(normalized) || hasRecentConsultContext(history)) {
            return IntentType.CONSULT;
        }
        return IntentType.CHAT;
    }

    private boolean isClearlyGeneralTask(String input) {
        if (RiskLexicon.hasConsultSignal(input)) {
            return false;
        }
        return GENERAL_TASK_WORDS.stream().anyMatch(input::contains);
    }

    private boolean hasRecentConsultContext(List<AiMessage> history) {
        if (history == null || history.isEmpty()) {
            return false;
        }
        return history.stream()
                .skip(Math.max(0, history.size() - 6))
                .map(message -> message.content().toLowerCase())
                .anyMatch(RiskLexicon::hasConsultSignal);
    }
}
