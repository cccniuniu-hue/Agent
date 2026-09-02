package com.mindbridge.agent.service.ai;

import java.util.List;

/**
 * 风险和咨询关键词词库。
 *
 * <p>它是模型判断前的硬规则兜底，主要用于快速识别明确危险信号。</p>
 */
public final class RiskLexicon {

    private static final List<String> HIGH_RISK_WORDS = List.of(
            "不想活", "活不下去", "撑不下去", "自杀", "自残", "轻生", "结束生命", "结束这一切",
            "伤害自己", "伤人", "杀了", "想死", "去死", "没有活着的意义", "不想存在", "消失算了",
            "suicide", "kill myself", "self harm", "end my life", "hurt myself", "hurt others", "want to die"
    );

    private static final List<String> CONSULT_WORDS = List.of(
            "焦虑", "压力", "压抑", "抑郁", "低落", "失眠", "睡不着", "崩溃", "难过", "孤独",
            "情绪", "心理", "心理咨询", "咨询师", "心累", "烦躁", "紧张", "心慌", "恐慌", "睡眠",
            "害怕", "恐惧", "内耗", "想哭",
            "不开心", "没动力", "痛苦", "沮丧", "绝望", "无助", "喘不过气", "panic attack",
            "anxiety", "anxious", "stress", "depress", "sad", "insomnia", "panic", "lonely", "breakup"
    );

    private RiskLexicon() {
    }

    public static boolean hasHighRiskSignal(String text) {
        return containsAny(text, HIGH_RISK_WORDS);
    }

    public static boolean hasConsultSignal(String text) {
        return containsAny(text, CONSULT_WORDS);
    }

    private static boolean containsAny(String text, List<String> words) {
        for (String word : words) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
