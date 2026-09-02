package com.mindbridge.agent.service.knowledge;

import com.mindbridge.agent.domain.KnowledgeChunk;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Lightweight BM25 scorer for keyword retrieval.
 */
public class Bm25Scorer {

    private static final double K1 = 1.5;
    private static final double B = 0.75;

    public List<SearchResult> rank(String query, List<KnowledgeChunk> chunks, int limit) {
        if (limit <= 0 || query == null || query.isBlank() || chunks.isEmpty()) {
            return List.of();
        }
        List<String> queryTerms = tokenize(query);
        if (queryTerms.isEmpty()) {
            return List.of();
        }
        CorpusStats stats = buildCorpusStats(chunks);
        return chunks.stream()
                .map(chunk -> new SearchResult(
                        chunk.getId(),
                        chunk.getSource(),
                        chunk.getContent(),
                        score(queryTerms, stats, chunk)))
                .filter(result -> result.score() > 0.0)
                .sorted(Comparator.comparingDouble(SearchResult::score).reversed())
                .limit(limit)
                .toList();
    }

    private CorpusStats buildCorpusStats(List<KnowledgeChunk> chunks) {
        Map<Long, Map<String, Integer>> termFrequencies = new HashMap<>();
        Map<Long, Integer> lengths = new HashMap<>();
        Map<String, Integer> documentFrequencies = new HashMap<>();
        int totalLength = 0;

        for (KnowledgeChunk chunk : chunks) {
            List<String> terms = tokenize(chunk.getContent());
            Map<String, Integer> frequencies = new HashMap<>();
            for (String term : terms) {
                frequencies.merge(term, 1, Integer::sum);
            }
            Long key = chunk.getId();
            termFrequencies.put(key, frequencies);
            lengths.put(key, terms.size());
            totalLength += terms.size();

            Set<String> uniqueTerms = new HashSet<>(terms);
            for (String term : uniqueTerms) {
                documentFrequencies.merge(term, 1, Integer::sum);
            }
        }

        double averageLength = chunks.isEmpty() ? 0.0 : totalLength / (double) chunks.size();
        return new CorpusStats(chunks.size(), averageLength, termFrequencies, lengths, documentFrequencies);
    }

    private double score(List<String> queryTerms, CorpusStats stats, KnowledgeChunk chunk) {
        Map<String, Integer> frequencies = stats.termFrequencies().getOrDefault(chunk.getId(), Map.of());
        int length = stats.lengths().getOrDefault(chunk.getId(), 0);
        if (frequencies.isEmpty() || length == 0 || stats.averageLength() == 0.0) {
            return 0.0;
        }

        double score = 0.0;
        for (String term : new HashSet<>(queryTerms)) {
            int tf = frequencies.getOrDefault(term, 0);
            if (tf == 0) {
                continue;
            }
            int df = stats.documentFrequencies().getOrDefault(term, 0);
            double idf = Math.log(1.0 + (stats.documentCount() - df + 0.5) / (df + 0.5));
            double denominator = tf + K1 * (1.0 - B + B * length / stats.averageLength());
            score += idf * (tf * (K1 + 1.0)) / denominator;
        }
        return score;
    }

    private List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String normalized = text.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        List<String> terms = new ArrayList<>();
        for (String token : normalized.split("[^\\p{IsHan}a-z0-9]+")) {
            if (!token.isBlank()) {
                terms.add(token);
            }
        }
        for (int i = 0; i < normalized.length() - 1; i++) {
            char first = normalized.charAt(i);
            char second = normalized.charAt(i + 1);
            if (isChinese(first) && isChinese(second)) {
                terms.add("" + first + second);
            }
        }
        return terms;
    }

    private boolean isChinese(char value) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(value);
        return block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B;
    }

    private record CorpusStats(
            int documentCount,
            double averageLength,
            Map<Long, Map<String, Integer>> termFrequencies,
            Map<Long, Integer> lengths,
            Map<String, Integer> documentFrequencies
    ) {
    }
}
