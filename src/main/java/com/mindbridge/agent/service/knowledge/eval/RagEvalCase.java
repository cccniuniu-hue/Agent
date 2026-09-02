package com.mindbridge.agent.service.knowledge.eval;

import java.util.List;

public record RagEvalCase(
        String id,
        String question,
        List<String> expectedSources,
        List<String> expectedTerms,
        String referenceAnswer,
        String expectedIntent,
        String expectedRiskLevel,
        List<String> groundedAnswerTerms,
        int minGroundedAnswerTerms,
        List<String> requiredAnswerTerms,
        List<String> requiredHelpTerms,
        List<String> forbiddenAnswerTerms
) {
}
