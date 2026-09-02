package com.mindbridge.agent.harness;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindbridge.agent.service.knowledge.eval.RagEvalCase;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

class RagEvaluationDatasetTests {

    private static final String DATASET_PATH = "rag-eval/mindbridge-rag-eval.json";

    @Test
    void defaultDatasetContainsOneHundredCompleteUniqueCasesCoveringAllKnowledgeSources() throws Exception {
        List<RagEvalCase> cases = loadCases();

        assertThat(cases).hasSize(100);
        assertThat(cases).extracting(RagEvalCase::id).doesNotHaveDuplicates();
        assertThat(cases).extracting(RagEvalCase::question).doesNotHaveDuplicates();
        assertThat(cases).filteredOn(testCase -> "LOW".equals(testCase.expectedRiskLevel())).hasSize(45);
        assertThat(cases).filteredOn(testCase -> "MEDIUM".equals(testCase.expectedRiskLevel())).hasSize(40);
        assertThat(cases).filteredOn(testCase -> "HIGH".equals(testCase.expectedRiskLevel())).hasSize(15);
        assertThat(cases).allSatisfy(testCase -> {
            assertThat(testCase.id()).isNotBlank();
            assertThat(testCase.question()).isNotBlank();
            assertThat(testCase.referenceAnswer()).isNotBlank();
            assertThat(testCase.expectedIntent()).isIn("CHAT", "CONSULT", "RISK");
            assertThat(testCase.expectedRiskLevel()).isIn("LOW", "MEDIUM", "HIGH");
            assertThat(testCase.expectedSources()).isNotNull().isNotEmpty();
            assertThat(testCase.expectedTerms()).isNotNull().isNotEmpty();
            assertThat(testCase.groundedAnswerTerms()).isNotNull();
            assertThat(testCase.requiredAnswerTerms()).isNotNull();
            assertThat(testCase.requiredHelpTerms()).isNotNull();
            assertThat(testCase.forbiddenAnswerTerms()).isNotNull();
            assertThat(testCase.minGroundedAnswerTerms()).isBetween(0, testCase.groundedAnswerTerms().size());
        });

        Set<String> expectedSources = cases.stream()
                .flatMap(testCase -> testCase.expectedSources().stream())
                .collect(Collectors.toSet());
        assertThat(expectedSources).containsExactlyInAnyOrderElementsOf(bundledKnowledgeSources());
    }

    private List<RagEvalCase> loadCases() throws Exception {
        Resource resource = new ClassPathResource(DATASET_PATH);
        try (InputStream inputStream = resource.getInputStream()) {
            return new ObjectMapper().readValue(inputStream, new TypeReference<>() {
            });
        }
    }

    private Set<String> bundledKnowledgeSources() throws Exception {
        Resource[] resources = new PathMatchingResourcePatternResolver()
                .getResources("classpath*:knowledge/*.md");
        return Arrays.stream(resources)
                .map(Resource::getFilename)
                .collect(Collectors.toSet());
    }
}
