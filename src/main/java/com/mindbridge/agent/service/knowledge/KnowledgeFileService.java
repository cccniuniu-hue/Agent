package com.mindbridge.agent.service.knowledge;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

@Service
/**
 * 管理员文件上传知识库服务。
 *
 * <p>负责文件大小校验、类型识别和文本抽取，抽取后的文本交给 KnowledgeService 处理。</p>
 */
public class KnowledgeFileService {

    private static final int MAX_FILE_BYTES = 10 * 1024 * 1024;

    private final KnowledgeService knowledgeService;

    public KnowledgeFileService(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    public int ingest(String filename, byte[] bytes) {
        // 文件上传入口只负责校验和抽取文本，真正切块、向量化、落库交给 KnowledgeService。
        if (bytes.length == 0) {
            throw new IllegalArgumentException("文件内容为空");
        }
        if (bytes.length > MAX_FILE_BYTES) {
            throw new IllegalArgumentException("文件不能超过 10MB");
        }
        String source = sanitizeSource(filename);
        String text = extractText(source, bytes);
        if (text.isBlank()) {
            throw new IllegalArgumentException("没有从文件中解析出可用文本");
        }
        return knowledgeService.ingest(source, text);
    }

    private String extractText(String filename, byte[] bytes) {
        String lower = filename.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".pdf")) {
            return extractPdf(bytes);
        }
        // Markdown 和 txt 都按 UTF-8 文本处理，适合管理员维护轻量知识库。
        if (lower.endsWith(".md") || lower.endsWith(".markdown") || lower.endsWith(".txt")) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
        throw new IllegalArgumentException("仅支持 PDF、Markdown 和 txt 文件");
    }

    private String extractPdf(byte[] bytes) {
        try (PDDocument document = Loader.loadPDF(bytes)) {
            return new PDFTextStripper().getText(document);
        } catch (Exception exception) {
            throw new IllegalArgumentException("PDF 文本解析失败：" + exception.getMessage());
        }
    }

    private String sanitizeSource(String filename) {
        String source = filename == null || filename.isBlank() ? "uploaded-knowledge" : filename.trim();
        // source 会进入数据库和后台列表，去掉路径分隔符避免显示本地路径。
        source = source.replaceAll("[\\\\/]+", "-");
        return source.length() > 180 ? source.substring(source.length() - 180) : source;
    }
}
