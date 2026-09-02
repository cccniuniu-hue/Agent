package com.mindbridge.agent.service.knowledge;

import com.mindbridge.agent.config.MindBridgeProperties;
import com.mindbridge.agent.repository.KnowledgeChunkRepository;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

@Service
/**
 * 内置知识库初始化服务。
 *
 * <p>启动时补齐 classpath:knowledge 下的默认文档；已有上传文档不会被清空。</p>
 */
public class KnowledgeIngestionService {

    private final KnowledgeChunkRepository knowledgeChunkRepository;
    private final KnowledgeService knowledgeService;
    private final MindBridgeProperties properties;
    private final KnowledgeChunker chunker = new KnowledgeChunker();

    public KnowledgeIngestionService(
            KnowledgeChunkRepository knowledgeChunkRepository,
            KnowledgeService knowledgeService,
            MindBridgeProperties properties
    ) {
        this.knowledgeChunkRepository = knowledgeChunkRepository;
        this.knowledgeService = knowledgeService;
        this.properties = properties;
    }

    public void syncClasspathKnowledge() {
        try {
            // classpath*: 支持未来从多个 jar 或目录中合并加载知识文件。
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath*:knowledge/*.*");
            for (Resource resource : resources) {
                String source = resource.getFilename();
                if (source == null || source.isBlank()) {
                    continue;
                }
                String content = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
                if (shouldIngestBundledSource(source, content)) {
                    knowledgeService.ingest(source, content);
                }
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to load bundled knowledge base", exception);
        }
    }

    private boolean shouldIngestBundledSource(String source, String content) {
        long existingChunks = knowledgeChunkRepository.countBySource(source);
        if (existingChunks == 0) {
            return true;
        }
        int expectedChunks = chunker.chunk(
                content,
                properties.getKnowledge().getChunkSize(),
                properties.getKnowledge().getChunkOverlap()).size();
        return existingChunks != expectedChunks;
    }
}
