package com.mindbridge.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 管理员通过 JSON 追加知识库内容的请求体。
 */
public record KnowledgeIngestRequest(
        @NotBlank @Size(max = 180) String source,
        @NotBlank String content
) {
}
