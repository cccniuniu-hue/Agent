package com.mindbridge.agent.service.agent;

import com.mindbridge.agent.domain.IntentType;
import com.mindbridge.agent.domain.RiskLevel;
import com.mindbridge.agent.service.PsychologicalAssessmentService;
import com.mindbridge.agent.service.PsychologyAssessment;
import org.springframework.stereotype.Component;

/**
 * 风险守护 Agent。
 *
 * <p>负责后台心理状态评估和高风险硬兜底；安全判断不能完全交给自由生成模型。</p>
 */
@Component
public class RiskGuardianAgent implements MindBridgeAgent {

    private final PsychologicalAssessmentService assessmentService;

    public RiskGuardianAgent(PsychologicalAssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @Override
    public AgentName name() {
        return AgentName.RISK_GUARDIAN_AGENT;
    }

    @Override
    public boolean supports(AgentContext context) {
        return context.knowledgeHandled()
                && !context.riskAssessed()
                && context.intent() != IntentType.CHAT;
    }

    @Override
    public AgentDecision act(AgentContext context) {
        PsychologyAssessment assessment = assessmentService.assess(context.modelInput(), context.modelHistory());
        if (context.intent() == IntentType.RISK && assessment.risk() != RiskLevel.HIGH) {
            assessment = new PsychologyAssessment(
                    assessment.emotion(),
                    Math.max(assessment.emotionScore(), 4.0),
                    RiskLevel.HIGH,
                    assessment.confidence(),
                    assessment.summary());
        }
        context.setAssessment(assessment);
        context.setRiskLevel(assessment.risk());
        context.markRiskAssessed();
        return AgentDecision.continueWith(
                AgentAction.ASSESS_RISK,
                "risk=%s, emotion=%s".formatted(assessment.risk(), assessment.emotion()));
    }
}
