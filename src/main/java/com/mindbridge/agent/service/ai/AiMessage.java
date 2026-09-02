package com.mindbridge.agent.service.ai;

/**
 * 发送给模型的一条消息。
 *
 * <p>role 使用 OpenAI/Ollama 兼容格式：system、user、assistant。</p>
 */
public record AiMessage(String role, String content) {

    public static AiMessage system(String content) {
        return new AiMessage("system", content);
    }

    public static AiMessage user(String content) {
        return new AiMessage("user", content);
    }

    public static AiMessage assistant(String content) {
        return new AiMessage("assistant", content);
    }
}
