package com.mindbridge.agent.dto;

/**
 * 知识库入库结果，返回数据来源和切块数量。
 */
public record KnowledgeIngestResponse(String source, int chunks) {
}
