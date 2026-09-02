package com.mindbridge.agent.service.agent;

/**
 * MindBridge 专业 Agent 接口。
 *
 * <p>每个 Agent 只负责一个清晰职责，由 AgentRuntimeService 按上下文状态循环选择下一步。</p>
 */
public interface MindBridgeAgent {

    AgentName name();

    boolean supports(AgentContext context);

    AgentDecision act(AgentContext context);
}
