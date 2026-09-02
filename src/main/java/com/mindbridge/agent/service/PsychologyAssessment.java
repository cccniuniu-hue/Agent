package com.mindbridge.agent.service;

import com.mindbridge.agent.domain.EmotionLabel;
import com.mindbridge.agent.domain.RiskLevel;

/**
 * 一次后台心理状态评估结果。
 *
 * <p>该对象只在服务端报告和工具链中使用，不作为学生端消息内容。</p>
 */
public record PsychologyAssessment(
        EmotionLabel emotion,
        double emotionScore,
        RiskLevel risk,
        double confidence,
        String summary
) {
}
