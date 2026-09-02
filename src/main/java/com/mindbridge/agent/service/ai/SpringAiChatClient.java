package com.mindbridge.agent.service.ai;

import java.util.List;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

/**
 * 基于 Spring AI 的聊天模型适配器。
 *
 * <p>项目内部继续使用简洁的 {@link AiClient} 接口；真正的大模型调用由
 * Spring AI 的 ChatModel / StreamingChatModel 完成。</p>
 */
public class SpringAiChatClient implements AiClient {

    private final ChatModel chatModel;
    private final StreamingChatModel streamingChatModel;

    public SpringAiChatClient(ChatModel chatModel, StreamingChatModel streamingChatModel) {
        this.chatModel = chatModel;
        this.streamingChatModel = streamingChatModel;
    }

    @Override
    public String complete(List<AiMessage> messages) {
        ChatResponse response = chatModel.call(new Prompt(toSpringMessages(messages)));
        return extractText(response);
    }

    @Override
    public Flux<String> stream(List<AiMessage> messages) {
        return streamingChatModel.stream(new Prompt(toSpringMessages(messages)))
                .map(this::extractText)
                .filter(token -> !token.isBlank());
    }

    private List<Message> toSpringMessages(List<AiMessage> messages) {
        return messages.stream()
                .map(this::toSpringMessage)
                .toList();
    }

    private Message toSpringMessage(AiMessage message) {
        return switch (message.role()) {
            case "system" -> new SystemMessage(message.content());
            case "assistant" -> new AssistantMessage(message.content());
            default -> new UserMessage(message.content());
        };
    }

    private String extractText(ChatResponse response) {
        if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
            return "";
        }
        String text = response.getResult().getOutput().getText();
        return text == null ? "" : text;
    }
}
