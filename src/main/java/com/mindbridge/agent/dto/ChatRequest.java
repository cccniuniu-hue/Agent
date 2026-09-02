package com.mindbridge.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 学生发起聊天请求。
 *
 * @param sessionId 为空时创建新会话；非空时继续已有会话
 * @param message 学生本轮输入
 */
public record ChatRequest(
        String sessionId,
        @NotBlank @Size(max = 4000) String message
) {
}
