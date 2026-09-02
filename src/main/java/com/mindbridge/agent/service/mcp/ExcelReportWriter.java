package com.mindbridge.agent.service.mcp;

import com.mindbridge.agent.domain.PsychologicalReport;

/**
 * 心理报告写入 Excel 的工具接口。
 *
 * <p>本地文件写入和远程 MCP 写入都实现这个接口。</p>
 */
public interface ExcelReportWriter {

    void write(PsychologicalReport report);
}
