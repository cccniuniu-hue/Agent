package com.mindbridge.agent.config;

import com.mindbridge.agent.service.mcp.AlertNotifier;
import com.mindbridge.agent.service.mcp.ExcelReportWriter;
import com.mindbridge.agent.service.mcp.HttpAlertNotifier;
import com.mindbridge.agent.service.mcp.HttpExcelReportWriter;
import com.mindbridge.agent.service.mcp.LocalExcelReportWriter;
import com.mindbridge.agent.service.mcp.LogAlertNotifier;
import com.mindbridge.agent.service.mcp.McpAlertNotifier;
import com.mindbridge.agent.service.mcp.McpExcelReportWriter;
import com.mindbridge.agent.service.mcp.McpToolClient;
import com.mindbridge.agent.service.mcp.MindBridgeMcpTools;
import com.mindbridge.agent.service.mcp.SmtpAlertNotifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
/**
 * MCP 工具链配置。
 *
 * <p>这里按配置选择 Excel 写入方式和预警通知方式，同时提供独立线程池，
 * 避免后台工具调用阻塞学生端聊天。</p>
 */
public class McpToolConfig {

    @Bean
    public TaskExecutor mcpTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("mindbridge-mcp-");
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(6);
        executor.setQueueCapacity(200);
        executor.initialize();
        return executor;
    }

    @Bean
    public ToolCallbackProvider mindBridgeMcpToolProvider(MindBridgeMcpTools mindBridgeMcpTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(mindBridgeMcpTools)
                .build();
    }

    @Bean
    public McpToolClient mcpToolClient(
            ObjectProvider<SyncMcpToolCallbackProvider> toolCallbackProvider,
            ObjectMapper objectMapper
    ) {
        return new McpToolClient(toolCallbackProvider, objectMapper);
    }

    @Bean
    public ExcelReportWriter excelReportWriter(
            MindBridgeProperties properties,
            WebClient.Builder webClientBuilder,
            McpToolClient mcpToolClient
    ) {
        String mode = properties.getMcp().getExcel().getMode();
        if ("mcp".equalsIgnoreCase(mode)) {
            return new McpExcelReportWriter(mcpToolClient);
        }
        if ("http".equalsIgnoreCase(mode)) {
            return new HttpExcelReportWriter(webClientBuilder, properties);
        }
        return new LocalExcelReportWriter(properties);
    }

    @Bean
    public AlertNotifier alertNotifier(
            MindBridgeProperties properties,
            WebClient.Builder webClientBuilder,
            JavaMailSender mailSender,
            McpToolClient mcpToolClient
    ) {
        String mode = properties.getMcp().getEmail().getMode();
        if ("mcp".equalsIgnoreCase(mode)) {
            return new McpAlertNotifier(mcpToolClient);
        }
        if ("http".equalsIgnoreCase(mode)) {
            return new HttpAlertNotifier(webClientBuilder, properties);
        }
        if ("smtp".equalsIgnoreCase(mode)) {
            return new SmtpAlertNotifier(mailSender, properties);
        }
        return new LogAlertNotifier();
    }
}
