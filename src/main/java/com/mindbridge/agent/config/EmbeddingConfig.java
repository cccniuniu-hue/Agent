package com.mindbridge.agent.config;

import com.mindbridge.agent.service.knowledge.EmbeddingClient;
import com.mindbridge.agent.service.knowledge.OpenAiEmbeddingClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
/**
 * RAG 向量化客户端配置。
 *
 * <p>KnowledgeService 通过 EmbeddingClient 获取文本向量；如果没有配置 API Key，
 * 客户端会返回空向量并触发本地检索兜底。</p>
 */
public class EmbeddingConfig {

    @Bean
    public EmbeddingClient embeddingClient(
            MindBridgeProperties properties,
            WebClient.Builder webClientBuilder
    ) {
        return new OpenAiEmbeddingClient(properties, webClientBuilder);
    }
}
