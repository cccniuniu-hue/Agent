package com.mindbridge.agent.service.mcp;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
public class MindBridgeMcpTools {

    private final McpToolExecutionService executionService;

    public MindBridgeMcpTools(McpToolExecutionService executionService) {
        this.executionService = executionService;
    }

    @Tool(
            name = McpToolNames.WRITE_EXCEL_REPORT,
            description = "Write a MindBridge psychological risk report to the configured Excel workbook."
    )
    public String writeExcelReport(
            @ToolParam(description = "Internal report id") Long reportId,
            @ToolParam(description = "Internal user id") Long userId,
            @ToolParam(description = "Student username") String username,
            @ToolParam(description = "Public chat session id") String sessionId,
            @ToolParam(description = "Classified intent") String intent,
            @ToolParam(description = "Classified emotion label") String emotion,
            @ToolParam(description = "Emotion score") double emotionScore,
            @ToolParam(description = "Risk level") String riskLevel,
            @ToolParam(description = "Assessment confidence") double confidence,
            @ToolParam(description = "Risk assessment summary") String summary,
            @ToolParam(description = "Original conversation content") String content,
            @ToolParam(description = "Report creation timestamp") String createdAt
    ) {
        return executionService.writeExcelReport(
                reportId,
                userId,
                username,
                sessionId,
                intent,
                emotion,
                emotionScore,
                riskLevel,
                confidence,
                summary,
                content,
                createdAt);
    }

    @Tool(
            name = McpToolNames.SEND_RISK_ALERT,
            description = "Send a MindBridge high-risk psychological alert to a counselor or administrator."
    )
    public String sendRiskAlert(
            @ToolParam(description = "Alert recipient email address") String recipient,
            @ToolParam(description = "Internal report id") Long reportId,
            @ToolParam(description = "Internal user id") Long userId,
            @ToolParam(description = "Student username") String username,
            @ToolParam(description = "Student display name") String displayName,
            @ToolParam(description = "Risk level") String riskLevel,
            @ToolParam(description = "Classified emotion label") String emotion,
            @ToolParam(description = "Emotion score") double emotionScore,
            @ToolParam(description = "Risk assessment summary") String summary,
            @ToolParam(description = "Original conversation content") String content
    ) {
        return executionService.sendRiskAlert(
                recipient,
                reportId,
                userId,
                username,
                displayName,
                riskLevel,
                emotion,
                emotionScore,
                summary,
                content);
    }
}
