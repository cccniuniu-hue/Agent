package com.mindbridge.agent.dto;

import com.mindbridge.agent.domain.AlertRecord;
import com.mindbridge.agent.domain.RiskLevel;
import com.mindbridge.agent.domain.ToolStatus;
import java.time.Instant;

/**
 * 管理员后台邮件/预警记录响应。
 */
public record AlertRecordResponse(
        Long id,
        Long reportId,
        Long userId,
        String username,
        String sessionId,
        RiskLevel riskLevel,
        String summary,
        String recipient,
        ToolStatus status,
        String errorMessage,
        int attempts,
        Instant createdAt,
        Instant updatedAt
) {
    public static AlertRecordResponse from(AlertRecord alert) {
        var report = alert.getReport();
        return new AlertRecordResponse(
                alert.getId(),
                report.getId(),
                report.getUser().getId(),
                report.getUser().getUsername(),
                report.getSession() == null ? null : report.getSession().getPublicId(),
                report.getRiskLevel(),
                report.getSummary(),
                alert.getRecipient(),
                alert.getStatus(),
                alert.getErrorMessage(),
                alert.getAttempts(),
                alert.getCreatedAt(),
                alert.getUpdatedAt());
    }
}
