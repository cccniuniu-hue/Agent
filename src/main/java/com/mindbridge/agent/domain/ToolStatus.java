package com.mindbridge.agent.domain;

/**
 * Excel 写入、邮件通知等外部工具的执行状态。
 */
public enum ToolStatus {
    PENDING,
    SUCCESS,
    FAILED,
    SKIPPED
}
