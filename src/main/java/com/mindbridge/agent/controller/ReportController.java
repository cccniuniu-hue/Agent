package com.mindbridge.agent.controller;

import com.mindbridge.agent.dto.ConversationResponse;
import com.mindbridge.agent.dto.AlertRecordResponse;
import com.mindbridge.agent.dto.AgentRunTraceResponse;
import com.mindbridge.agent.dto.AgentRunTraceSummaryResponse;
import com.mindbridge.agent.dto.ExcelRecordResponse;
import com.mindbridge.agent.dto.ReportResponse;
import com.mindbridge.agent.security.CurrentUser;
import com.mindbridge.agent.service.AgentRunTraceService;
import com.mindbridge.agent.service.ReportService;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
/**
 * 报告、Excel 记录、邮件记录和完整会话查询接口。
 *
 * <p>管理员后台的数据列表和详情弹窗主要由这些接口驱动。</p>
 */
public class ReportController {

    private final ReportService reportService;
    private final AgentRunTraceService agentRunTraceService;

    public ReportController(ReportService reportService, AgentRunTraceService agentRunTraceService) {
        this.reportService = reportService;
        this.agentRunTraceService = agentRunTraceService;
    }

    @GetMapping("/reports/me")
    public List<ReportResponse> myReports(@AuthenticationPrincipal CurrentUser currentUser) {
        return reportService.myReports(currentUser.getId()).stream()
                .map(ReportResponse::from)
                .toList();
    }

    @GetMapping("/admin/reports")
    public List<ReportResponse> latestReports() {
        // 管理员统计大屏使用这个接口作为对话报告主数据源。
        return reportService.latestReports().stream()
                .map(ReportResponse::from)
                .toList();
    }

    @GetMapping("/admin/excel-records")
    public List<ExcelRecordResponse> excelRecords() {
        return reportService.excelRecords();
    }

    @GetMapping("/admin/alerts")
    public List<AlertRecordResponse> alertRecords() {
        return reportService.alertRecords();
    }

    @GetMapping("/admin/conversations/{sessionId}")
    public ConversationResponse conversation(@PathVariable String sessionId) {
        // 点开任一后台记录时读取完整会话，便于辅导员回看上下文。
        return reportService.conversation(sessionId);
    }

    @GetMapping("/admin/conversations/{sessionId}/run-traces")
    public List<AgentRunTraceResponse> conversationRunTraces(@PathVariable String sessionId) {
        return agentRunTraceService.tracesForSession(sessionId);
    }

    @GetMapping("/admin/run-traces")
    public List<AgentRunTraceSummaryResponse> latestRunTraces() {
        return agentRunTraceService.latestTraces();
    }

    @GetMapping("/admin/run-traces/{traceId}")
    public AgentRunTraceResponse runTrace(@PathVariable String traceId) {
        return agentRunTraceService.trace(traceId);
    }
}
