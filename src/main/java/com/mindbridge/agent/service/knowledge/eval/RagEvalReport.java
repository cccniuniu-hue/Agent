package com.mindbridge.agent.service.knowledge.eval;

import java.time.Instant;
import java.util.List;

public record RagEvalReport(
        Instant evaluatedAt,
        String dataset,
        int topK,
        int totalCases,
        long passedCases,
        long failedCases,
        List<RagEndToEndCaseResult> cases
) {
}
