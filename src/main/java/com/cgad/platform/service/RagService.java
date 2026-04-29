package com.cgad.platform.service;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG 检索增强生成服务
 *
 * 解决通用大模型的核心问题：
 *   - 知识缺失：大模型的训练数据有截止日期，不知道企业私有数据
 *   - 幻觉问题：大模型可能编造看似合理但实际错误的信息
 *
 * RAG 的解决方案：
 *   - 先从向量数据库中检索与问题相关的文档片段
 *   - 将检索结果作为上下文注入到 Prompt 中
 *   - 大模型基于真实文档内容生成回答，大幅减少幻觉
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final ChatLanguageModel chatModel;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    private static final int MAX_RESULTS = 5;
    private static final double MIN_SCORE = 0.6;

    /**
     * 基于检索增强的问答
     *
     * 流程：
     *   1. 将用户问题向量化
     *   2. 在 PGVector 中搜索最相似的文档片段
     *   3. 将检索到的片段组装为上下文
     *   4. 构建 Prompt：上下文 + 问题 → LLM 生成回答
     */
    public String askWithRag(String question) {
        List<EmbeddingMatch<TextSegment>> relevant = retrieveRelevant(question);

        if (relevant.isEmpty()) {
            log.warn("No relevant documents found for question, falling back to direct LLM");
            return chatModel.chat(question);
        }

        String context = relevant.stream()
                .map(match -> match.embedded().text())
                .collect(Collectors.joining("\n\n---\n\n"));

        String prompt = buildRagPrompt(context, question);
        log.info("RAG prompt built with {} relevant segments", relevant.size());

        return chatModel.chat(prompt);
    }

    /**
     * 检索与问题相关的文档片段
     */
    public List<EmbeddingMatch<TextSegment>> retrieveRelevant(String query) {
        var queryEmbedding = embeddingModel.embed(query).content();

        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(
                queryEmbedding, MAX_RESULTS, MIN_SCORE
        );

        log.info("Found {} relevant segments for query (minScore={})", matches.size(), MIN_SCORE);
        return matches;
    }

    /**
     * 构建 RAG Prompt
     *
     * Prompt 设计要点：
     *   - 明确告诉模型只能基于提供的上下文回答
     *   - 如果上下文中没有相关信息，要求模型如实说明
     *   - 这能有效抑制大模型的幻觉问题
     */
    private String buildRagPrompt(String context, String question) {
        return """
                你是一个专业的文档分析助手。请基于以下检索到的文档内容回答用户的问题。
                
                要求：
                1. 只基于提供的文档内容回答，不要编造信息
                2. 如果文档中没有相关信息，请明确说明"根据已有文档，无法回答该问题"
                3. 引用文档内容时，尽量保持原文表述
                4. 回答要简洁、准确、有条理
                
                --- 检索到的文档内容 ---
                %s
                --- 文档内容结束 ---
                
                用户问题：%s
                """.formatted(context, question);
    }
}
