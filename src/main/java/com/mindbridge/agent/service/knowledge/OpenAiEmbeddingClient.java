package com.mindbridge.agent.service.knowledge;

import com.fasterxml.jackson.databind.JsonNode;
import com.mindbridge.agent.config.MindBridgeProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * OpenAI 兼容 embedding 客户端。
 *
 * <p>用于把上传知识和查询文本转换成向量；未配置 API Key 时保持无副作用返回空结果。</p>
 */
public class OpenAiEmbeddingClient implements EmbeddingClient {

    private final MindBridgeProperties properties;
    private final WebClient webClient;

    public OpenAiEmbeddingClient(MindBridgeProperties properties, WebClient.Builder webClientBuilder) {
        this.properties = properties;
        WebClient.Builder builder = webClientBuilder.baseUrl(properties.getEmbedding().getBaseUrl());
        if (!properties.getEmbedding().getApiKey().isBlank()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getEmbedding().getApiKey());
        }
        this.webClient = builder.build();
    }

    @Override
    public List<Double> embed(String text) {
        // API Key 为空时直接返回空向量，让 RAG 使用本地检索兜底。
        if (properties.getEmbedding().getApiKey().isBlank() || text == null || text.isBlank()) {
            return List.of();
        }
        Map<String, Object> body = Map.of(
                "model", properties.getEmbedding().getModel(),
                "input", text,
                "encoding_format", "float"
        );
        JsonNode response = webClient.post()
                .uri("/v1/embeddings")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
        JsonNode embedding = response == null
                ? null
                : response.path("data").path(0).path("embedding");
        // 服务端响应异常时不抛给业务层，返回空结果交给 KnowledgeService 回退。
        if (embedding == null || !embedding.isArray()) {
            return List.of();
        }
        List<Double> values = new ArrayList<>(embedding.size());
        embedding.forEach(value -> values.add(value.asDouble()));
        return values;
    }

    @Override
    public String modelName() {
        return properties.getEmbedding().getModel();
    }
}
