package com.mindbridge.agent.service.mcp;

import com.mindbridge.agent.domain.AlertRecord;
import com.mindbridge.agent.domain.PsychologicalReport;
import java.util.LinkedHashMap;
import java.util.Map;

public class McpAlertNotifier implements AlertNotifier {

    private final McpToolClient mcpToolClient;

    public McpAlertNotifier(McpToolClient mcpToolClient) {
        this.mcpToolClient = mcpToolClient;
    }

    @Override
    public void notify(AlertRecord alertRecord, PsychologicalReport report) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("recipient", alertRecord.getRecipient());
        payload.put("reportId", report.getId());
        payload.put("userId", report.getUser().getId());
        payload.put("username", report.getUser().getUsername());
        payload.put("displayName", report.getUser().getDisplayName());
        payload.put("riskLevel", report.getRiskLevel().name());
        payload.put("emotion", report.getEmotion().name());
        payload.put("emotionScore", report.getEmotionScore());
        payload.put("summary", report.getSummary());
        payload.put("content", report.getContent());
        mcpToolClient.call(McpToolNames.SEND_RISK_ALERT, payload);
    }
}
