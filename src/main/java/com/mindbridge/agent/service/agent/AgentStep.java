package com.mindbridge.agent.service.agent;

import java.time.Instant;

/**
 * Agent loop 的一步执行轨迹。
 *
 * <p>当前先保留在本轮运行结果中，后续如果要在管理员后台展示 agent trace，可以直接持久化这个对象。</p>
 */
public record AgentStep(
        int step,
        AgentName agent,
        AgentAction action,
        String observation,
        Instant createdAt
) {
    public static AgentStep of(int step, AgentName agent, AgentDecision decision) {
        return new AgentStep(step, agent, decision.action(), decision.observation(), Instant.now());
    }
}
