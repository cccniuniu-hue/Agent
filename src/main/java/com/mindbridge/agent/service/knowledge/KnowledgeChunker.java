package com.mindbridge.agent.service.knowledge;

import java.util.ArrayList;
import java.util.List;

/**
 * 知识库文本切块器。
 *
 * <p>优先在换行、句号和英文标点附近切分，减少单个片段语义被截断。</p>
 */
public class KnowledgeChunker {

    public List<String> chunk(String content, int chunkSize, int overlap) {
        String text = content.replace("\r\n", "\n").trim();
        if (text.isBlank()) {
            return List.of();
        }
        List<String> chunks = new ArrayList<>();
        int safeSize = Math.max(120, chunkSize);
        int safeOverlap = Math.max(0, Math.min(overlap, safeSize / 2));
        int index = 0;
        while (index < text.length()) {
            int end = Math.min(text.length(), index + safeSize);
            if (end < text.length()) {
                // 尽量在自然边界切开，找不到合适边界时才按固定长度切。
                int boundary = Math.max(
                        Math.max(text.lastIndexOf("\n", end), text.lastIndexOf("。", end)),
                        Math.max(text.lastIndexOf(".", end), text.lastIndexOf("?", end)));
                if (boundary > index + safeSize / 2) {
                    end = boundary + 1;
                }
            }
            chunks.add(text.substring(index, end).trim());
            if (end >= text.length()) {
                break;
            }
            index = Math.max(0, end - safeOverlap);
        }
        return chunks;
    }
}
