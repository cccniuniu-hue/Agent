package com.mindbridge.agent.harness;

import com.mindbridge.agent.service.ai.AiClient;
import com.mindbridge.agent.service.ai.AiMessage;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import reactor.core.publisher.Flux;

final class ScriptedAiClient implements AiClient {

    private final List<String> completePrompts = new CopyOnWriteArrayList<>();
    private final List<String> streamPrompts = new CopyOnWriteArrayList<>();

    @Override
    public String complete(List<AiMessage> messages) {
        String prompt = joined(messages);
        completePrompts.add(prompt);
        String normalized = prompt.toLowerCase(Locale.ROOT);
        if (prompt.contains("用户意图分类器")) {
            return classify(normalized);
        }
        if (prompt.contains("校园心理健康消息")) {
            return assessment(normalized);
        }
        if (prompt.contains("MemoryAgent")) {
            return "无相关历史记忆。";
        }
        if (prompt.contains("用户画像记忆抽取器")) {
            return "[]";
        }
        if (prompt.contains("KnowledgeAgent") && prompt.contains("改写成适合检索")) {
            return "校园心理中心 焦虑 睡眠 安全";
        }
        if (prompt.contains("判断检索结果是否足以")) {
            return "SUFFICIENT";
        }
        if (prompt.contains("RAG reranker")) {
            return "[{\"index\":1,\"score\":0.99},{\"index\":2,\"score\":0.70}]";
        }
        if (prompt.contains("CompanionAgent")) {
            return "直接回答用户的普通学习或生活问题，不引导成心理评估。";
        }
        if (prompt.contains("CounselorAgent")) {
            return "先共情，再给出具体支持步骤；高风险时优先确认安全和求助。";
        }
        if (prompt.contains("RAG 回答生成器")) {
            return ragAnswer(normalized);
        }
        return "好的，我会稳妥回应。";
    }

    @Override
    public Flux<String> stream(List<AiMessage> messages) {
        String prompt = joined(messages);
        streamPrompts.add(prompt);
        String normalized = prompt.toLowerCase(Locale.ROOT);
        if (containsAny(normalized, "不想活", "伤害自己", "计划伤害", "immediate danger")) {
            return Flux.just(
                    "先确保安全，",
                    "请联系身边可信任的人、辅导员或学校心理中心；",
                    "如果有立即危险，请拨打当地紧急救助。");
        }
        if (containsAny(normalized, "焦虑", "失眠", "心理中心")) {
            return Flux.just("先把困扰说清楚，", "再试试呼吸、睡眠作息调整，并联系学校心理中心。");
        }
        return Flux.just("这是一个稳定的测试回复。");
    }

    List<String> completePrompts() {
        return List.copyOf(completePrompts);
    }

    List<String> streamPrompts() {
        return List.copyOf(streamPrompts);
    }

    private String classify(String prompt) {
        String currentInput = currentInput(prompt);
        String recentContext = recentContext(prompt);
        if (containsAny(currentInput, "不想活", "伤害自己", "计划伤害", "自杀")) {
            return "RISK";
        }
        if (containsAny(currentInput, "焦虑", "失眠", "低落", "没动力", "心理中心", "抑郁")
                || containsAny(recentContext, "焦虑", "失眠", "低落", "没动力", "心理中心", "抑郁")) {
            return "CONSULT";
        }
        return "CHAT";
    }

    private String assessment(String prompt) {
        String currentInput = currentInput(prompt);
        if (containsAny(currentInput, "不想活", "伤害自己", "计划伤害", "自杀")) {
            return """
                    {"emotion":"HIGH_RISK","emotionScore":4.0,"risk":"HIGH","confidence":0.96,"summary":"明确高风险信号。"}
                    """;
        }
        if (containsAny(currentInput, "低落", "没动力", "影响上课", "影响吃饭", "连续几周")) {
            return """
                    {"emotion":"DEPRESSED","emotionScore":3.2,"risk":"MEDIUM","confidence":0.82,"summary":"持续困扰影响功能。"}
                    """;
        }
        if (containsAny(currentInput, "焦虑", "失眠", "心慌")) {
            return """
                    {"emotion":"ANXIETY","emotionScore":2.2,"risk":"LOW","confidence":0.78,"summary":"焦虑或睡眠困扰。"}
                    """;
        }
        return """
                {"emotion":"NORMAL","emotionScore":0.0,"risk":"LOW","confidence":0.70,"summary":"无明显风险信号。"}
                """;
    }

    private String ragAnswer(String prompt) {
        String question = studentQuestion(prompt);
        if (containsAny(question, "不想活", "伤害自己", "计划伤害", "今晚可能")) {
            return "先把安全放在第一位：请远离可能伤害自己的物品，马上联系身边可信任的人、辅导员或学校心理中心；如果有立即危险，请拨打当地紧急救助电话。";
        }
        if (containsAny(question, "焦虑", "心慌", "失眠")) {
            return "焦虑心慌时可以先做五感着陆和呼吸练习，同时照顾睡眠和作息；如果持续影响生活，建议联系学校心理中心。";
        }
        return "可以先记录困扰和持续时间，再联系可信任的人或学校心理中心获得支持。";
    }

    private String joined(List<AiMessage> messages) {
        return String.join("\n", messages.stream()
                .map(message -> message.role() + ": " + message.content())
                .toList());
    }

    private boolean containsAny(String text, String... needles) {
        for (String needle : needles) {
            if (text.contains(needle.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private String currentInput(String prompt) {
        return sectionAfter(prompt, "当前输入：");
    }

    private String recentContext(String prompt) {
        int start = prompt.lastIndexOf("最近上下文：");
        int end = prompt.lastIndexOf("当前输入：");
        if (start < 0 || end <= start) {
            return "";
        }
        return prompt.substring(start + "最近上下文：".length(), end)
                .toLowerCase(Locale.ROOT);
    }

    private String studentQuestion(String prompt) {
        return sectionAfter(prompt, "学生问题：");
    }

    private String sectionAfter(String prompt, String marker) {
        int start = prompt.lastIndexOf(marker);
        if (start < 0) {
            return prompt;
        }
        String section = prompt.substring(start + marker.length());
        int nextBlankLine = section.indexOf("\n\n");
        if (nextBlankLine >= 0) {
            section = section.substring(0, nextBlankLine);
        }
        return section.toLowerCase(Locale.ROOT).trim();
    }
}
