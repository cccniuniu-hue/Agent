package com.mindbridge.agent.service.mcp;

import com.mindbridge.agent.domain.AlertRecord;
import com.mindbridge.agent.domain.PsychologicalReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志预警实现。
 *
 * <p>用于本地演示或无 SMTP 环境时验证高风险链路是否被触发。</p>
 */
public class LogAlertNotifier implements AlertNotifier {

    private static final Logger log = LoggerFactory.getLogger(LogAlertNotifier.class);

    @Override
    public void notify(AlertRecord alertRecord, PsychologicalReport report) {
        log.warn(
                "High risk alert dry-run: recipient={}, reportId={}, user={}, summary={}",
                alertRecord.getRecipient(),
                report.getId(),
                report.getUser().getUsername(),
                report.getSummary());
    }
}
