package com.mindbridge.agent.service.memory;

import com.fasterxml.jackson.databind.JsonNode;
import com.mindbridge.agent.config.MindBridgeProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

/** 仅在画像专用配置显式启用且提供专用密钥时调用 embedding 服务。 */
public class ConfiguredMemoryEmbeddingClient implements MemoryEmbeddingClient {

    private final MindBridgeProperties.MemoryEmbedding config;
    private final WebClient webClient;

    public ConfiguredMemoryEmbeddingClient(MindBridgeProperties properties, WebClient.Builder webClientBuilder) {
        this.config = properties.getMemory().getEmbedding();
        WebClient.Builder builder = webClientBuilder.baseUrl(config.getBaseUrl());
        if (config.getApiKey() != null && !config.getApiKey().isBlank()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + config.getApiKey());
        }
        this.webClient = builder.build();
    }

    @Override
    public List<Double> embed(String text) {
        if (!config.isEnabled()
                || config.getApiKey() == null
                || config.getApiKey().isBlank()
                || config.getModel() == null
                || config.getModel().isBlank()
                || config.getDimensions() < 1
                || text == null
                || text.isBlank()) {
            return List.of();
        }
        try {
            JsonNode response = webClient.post()
                    .uri("/v1/embeddings")
                    .bodyValue(Map.of(
                            "model", config.getModel(),
                            "input", text,
                            "dimensions", config.getDimensions(),
                            "encoding_format", "float"))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            JsonNode embedding = response == null ? null : response.path("data").path(0).path("embedding");
            if (embedding == null || !embedding.isArray() || embedding.size() != config.getDimensions()) {
                return List.of();
            }
            List<Double> values = new ArrayList<>(embedding.size());
            embedding.forEach(value -> values.add(value.asDouble()));
            return values;
        } catch (Exception ignored) {
            return List.of();
        }
    }

    @Override
    public String modelName() {
        return config.getModel();
    }
}
