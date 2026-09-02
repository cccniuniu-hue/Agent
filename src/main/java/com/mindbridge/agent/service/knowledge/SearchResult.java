package com.mindbridge.agent.service.knowledge;

/**
 * RAG 检索结果。
 *
 * @param chunkId 数据库切块 id，外部检索结果没有 id 时可以为空
 * @param source 知识来源文件或来源名
 * @param content 命中的文本内容
 * @param score 检索相关性分数，越高越相关
 */
public record SearchResult(Long chunkId, String source, String content, double score) {
}
