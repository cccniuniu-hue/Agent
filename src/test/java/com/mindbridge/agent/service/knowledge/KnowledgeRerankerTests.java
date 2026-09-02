package com.mindbridge.agent.service.knowledge;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindbridge.agent.config.MindBridgeProperties;
import com.mindbridge.agent.service.ai.AiClient;
import com.mindbridge.agent.service.ai.AiMessage;
import java.util.List;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

class KnowledgeRerankerTests {

    @Test
    void reranksCandidatesUsingModelScores() {
        KnowledgeReranker reranker = new KnowledgeReranker(
                properties(),
                new StubAiClient("""
                        [
                          {"index": 1, "score": 0.10},
                          {"index": 2, "score": 0.98},
                          {"index": 3, "score": 0.40}
                        ]
                        """),
                new ObjectMapper());

        List<SearchResult> results = reranker.rerank("最近焦虑到睡不着，想知道怎么缓解", List.of(
                new SearchResult(1L, "a.md", "校园心理中心预约流程", 0.90),
                new SearchResult(2L, "b.md", "焦虑、入睡困难和睡前放松练习", 0.80),
                new SearchResult(3L, "c.md", "考试压力下的计划拆分", 0.20)
        ), 2);

        assertThat(results)
                .extracting(SearchResult::chunkId)
                .containsExactly(2L, 3L);
    }

    @Test
    void fallsBackToInitialOrderWhenModelOutputIsInvalid() {
        KnowledgeReranker reranker = new KnowledgeReranker(
                properties(),
                new StubAiClient("我觉得第二条更相关"),
                new ObjectMapper());

        List<SearchResult> results = reranker.rerank("焦虑睡眠", List.of(
                new SearchResult(1L, "a.md", "初排第一", 0.90),
                new SearchResult(2L, "b.md", "初排第二", 0.80),
                new SearchResult(3L, "c.md", "初排第三", 0.70)
        ), 2);

        assertThat(results)
                .extracting(SearchResult::chunkId)
                .containsExactly(1L, 2L);
    }

    private MindBridgeProperties properties() {
        MindBridgeProperties properties = new MindBridgeProperties();
        properties.getKnowledge().setRerankerEnabled(true);
        properties.getKnowledge().setRerankerCandidateLimit(3);
        properties.getKnowledge().setRerankerMaxContentChars(200);
        return properties;
    }

    private static class StubAiClient implements AiClient {

        private final String response;

        private StubAiClient(String response) {
            this.response = response;
        }

        @Override
        public String complete(List<AiMessage> messages) {
            return response;
        }

        @Override
        public Flux<String> stream(List<AiMessage> messages) {
            return Flux.empty();
        }
    }
}
