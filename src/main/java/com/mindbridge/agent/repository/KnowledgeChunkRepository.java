package com.mindbridge.agent.repository;

import com.mindbridge.agent.domain.KnowledgeChunk;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 知识库切块的数据访问接口。
 */
public interface KnowledgeChunkRepository extends JpaRepository<KnowledgeChunk, Long> {

    List<KnowledgeChunk> findTop20BySourceOrderByCreatedAtDesc(String source);

    /** 检索命中后取相邻切块，用于补齐上下文。 */
    List<KnowledgeChunk> findBySourceAndSourceIndexBetweenOrderBySourceIndexAsc(
            String source,
            int startIndex,
            int endIndex
    );

    /** 判断内置或上传来源是否已经入库。 */
    long countBySource(String source);

    /** 同名文件重新上传时清理旧切块。 */
    void deleteBySource(String source);
}
