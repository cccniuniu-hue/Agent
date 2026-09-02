package com.mindbridge.agent.service.agent;

/**
 * 单个 Agent 执行一步后返回的决策摘要。
 *
 * @param action 当前执行的动作
 * @param observation 动作结果摘要，用于后续调试和可视化 trace
 * @param complete 是否结束本轮 agent loop
 */
public record AgentDecision(
        AgentAction action,
        String observation,
        boolean complete
) {
    public static AgentDecision continueWith(AgentAction action, String observation) {
        return new AgentDecision(action, observation, false);
    }

    public static AgentDecision finish(AgentAction action, String observation) {
        return new AgentDecision(action, observation, true);
    }
}
