package com.mindbridge.agent.dto;

import com.mindbridge.agent.domain.ChatMessage;
import com.mindbridge.agent.domain.MessageRole;
import java.time.Instant;

/**
 * 管理员查看完整会话时的单条消息响应。
 */
public record ConversationMessageResponse(
        Long id,
        MessageRole role,
        String content,
        Instant createdAt
) {
    public static ConversationMessageResponse from(ChatMessage message) {
        return new ConversationMessageResponse(
                message.getId(),
                message.getRole(),
                message.getContent(),
                message.getCreatedAt());
    }
}
