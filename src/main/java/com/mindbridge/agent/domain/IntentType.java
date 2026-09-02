package com.mindbridge.agent.domain;

/**
 * 用户输入的业务意图。
 *
 * <p>CHAT 走普通对话，CONSULT/RISK 才进入心理支持、RAG 和报告链路。</p>
 */
public enum IntentType {
    CHAT,
    CONSULT,
    RISK
}
