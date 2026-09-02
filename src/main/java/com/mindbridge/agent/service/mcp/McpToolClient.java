package com.mindbridge.agent.service.mcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.ObjectProvider;

public class McpToolClient {

    private final ObjectProvider<SyncMcpToolCallbackProvider> toolCallbackProvider;
    private final ObjectMapper objectMapper;

    public McpToolClient(
            ObjectProvider<SyncMcpToolCallbackProvider> toolCallbackProvider,
            ObjectMapper objectMapper
    ) {
        this.toolCallbackProvider = toolCallbackProvider;
        this.objectMapper = objectMapper;
    }

    public String call(String toolName, Map<String, Object> arguments) {
        ToolCallback callback = findTool(toolName);
        try {
            return callback.call(objectMapper.writeValueAsString(arguments));
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Failed to serialize MCP tool arguments for " + toolName, exception);
        }
    }

    private ToolCallback findTool(String toolName) {
        SyncMcpToolCallbackProvider mcpToolCallbackProvider = toolCallbackProvider.getIfAvailable(() -> {
            throw new IllegalStateException(
                    "Spring AI MCP client is not available. Set MCP_CLIENT_ENABLED=true before using MCP tool mode.");
        });
        for (ToolCallback callback : mcpToolCallbackProvider.getToolCallbacks()) {
            if (toolName.equals(callback.getToolDefinition().name())) {
                return callback;
            }
        }
        throw new IllegalStateException("MCP tool is not available: " + toolName);
    }
}
