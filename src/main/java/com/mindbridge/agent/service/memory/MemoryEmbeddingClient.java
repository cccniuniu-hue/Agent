package com.mindbridge.agent.service.memory;

import java.util.List;

/** 用户画像专用向量化入口，与公共知识库的 EmbeddingClient 隔离。 */
public interface MemoryEmbeddingClient {

    List<Double> embed(String text);

    String modelName();
}
