package com.mindbridge.agent.service.knowledge;

import java.util.List;

/**
 * 文本向量化接口。
 *
 * <p>KnowledgeService 不直接依赖具体 embedding 服务，方便后续替换实现。</p>
 */
public interface EmbeddingClient {

    List<Double> embed(String text);

    String modelName();
}
