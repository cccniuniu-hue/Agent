package com.mindbridge.agent.service.mcp;

import com.mindbridge.agent.config.MindBridgeProperties;
import com.mindbridge.agent.domain.PsychologicalReport;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * 本地 Excel 文件写入实现。
 *
 * <p>适合演示环境使用，会把报告追加到 data 目录下的工作簿中。</p>
 */
public class LocalExcelReportWriter implements ExcelReportWriter {

    private final Path path;
    private final Object lock = new Object();

    public LocalExcelReportWriter(MindBridgeProperties properties) {
        this.path = Path.of(properties.getMcp().getExcel().getLocalPath());
    }

    @Override
    public void write(PsychologicalReport report) {
        synchronized (lock) {
            try {
                // 写文件需要串行化，防止多个高风险报告同时写入造成工作簿损坏。
                if (path.getParent() != null) {
                    Files.createDirectories(path.getParent());
                }
                Workbook workbook = openWorkbook();
                Sheet sheet = workbook.getNumberOfSheets() == 0
                        ? workbook.createSheet("reports")
                        : workbook.getSheetAt(0);
                if (sheet.getPhysicalNumberOfRows() == 0) {
                    writeHeader(sheet.createRow(0));
                }
                Row row = sheet.createRow(sheet.getLastRowNum() + 1);
                writeReport(row, report);
                for (int i = 0; i < 12; i++) {
                    sheet.autoSizeColumn(i);
                }
                try (OutputStream outputStream = Files.newOutputStream(path)) {
                    workbook.write(outputStream);
                }
                workbook.close();
            } catch (Exception exception) {
                throw new IllegalStateException("Failed to write local Excel report", exception);
            }
        }
    }

    private Workbook openWorkbook() throws Exception {
        if (!Files.exists(path)) {
            return new XSSFWorkbook();
        }
        // 已存在时追加到原工作簿，保留历史写入记录。
        try (InputStream inputStream = Files.newInputStream(path)) {
            return WorkbookFactory.create(inputStream);
        }
    }

    private void writeHeader(Row row) {
        String[] headers = {
                "报告ID", "用户ID", "账号", "会话ID", "意图", "情绪标签", "情绪总分",
                "风险等级", "置信度", "判断摘要", "对话内容", "对话时间"
        };
        for (int i = 0; i < headers.length; i++) {
            cell(row, i).setCellValue(headers[i]);
        }
    }

    private void writeReport(Row row, PsychologicalReport report) {
        cell(row, 0).setCellValue(nullSafe(report.getId()));
        cell(row, 1).setCellValue(nullSafe(report.getUser().getId()));
        cell(row, 2).setCellValue(report.getUser().getUsername());
        cell(row, 3).setCellValue(report.getSession() == null ? "" : report.getSession().getPublicId());
        cell(row, 4).setCellValue(report.getIntent().name());
        cell(row, 5).setCellValue(report.getEmotion().name());
        cell(row, 6).setCellValue(report.getEmotionScore());
        cell(row, 7).setCellValue(report.getRiskLevel().name());
        cell(row, 8).setCellValue(report.getConfidence());
        cell(row, 9).setCellValue(report.getSummary());
        cell(row, 10).setCellValue(report.getContent());
        cell(row, 11).setCellValue(report.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime().toString());
    }

    private Cell cell(Row row, int index) {
        return row.createCell(index);
    }

    private long nullSafe(Long value) {
        return value == null ? 0L : value;
    }
}
