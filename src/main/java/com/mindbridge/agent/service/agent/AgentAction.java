package com.mindbridge.agent.service.agent;

/**
 * Agent loop 中每一步可执行的动作。
 */
public enum AgentAction {
    READ_MEMORY,
    ROUTE_INTENT,
    RETRIEVE_KNOWLEDGE,
    ASSESS_RISK,
    PLAN_RESPONSE
}
