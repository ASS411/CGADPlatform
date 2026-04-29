package com.cgad.platform.service;

import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AiServices 结构化输出服务
 *
 * LangChain4j AiServices 的核心能力：
 *   - 通过 Java 接口定义 LLM 的输入输出契约
 *   - 自动将 LLM 的自由文本输出转换为 Java POJO
 *   - 无需手动解析 JSON，框架自动完成序列化/反序列化
 *
 * 与手动 Prompt + JSON 解析的区别：
 *   - 手动方式：构建 Prompt → 调用 LLM → 提取 JSON → Jackson 反序列化
 *   - AiServices：定义接口 + POJO → 框架自动处理全流程
 *   - AiServices 更类型安全、更简洁、更不容易出错
 */
@Slf4j
@Service
public class StructuredExtractionService {

    /**
     * 文档摘要提取接口
     *
     * @SystemMessage：系统提示词，设定 AI 的角色和行为
     * @UserMessage：用户消息模板，{{content}} 是占位符
     * 返回类型 DocumentSummary：POJO，框架自动将 LLM 输出映射到此对象
     */
    interface DocumentSummaryExtractor {
        @SystemMessage("你是一个专业的文档分析助手。请从文档中提取结构化摘要信息。必须严格按照JSON格式输出。")
        DocumentSummary extract(@UserMessage("请分析以下文档并提取摘要信息：\n{{content}}") String content);
    }

    /**
     * 情感分析接口
     */
    interface SentimentAnalyzer {
        @SystemMessage("你是一个文本情感分析专家。请分析文本的情感倾向，严格按照JSON格式输出。")
        SentimentResult analyze(@UserMessage("请分析以下文本的情感：\n{{content}}") String content);
    }

    /**
     * 文档摘要 POJO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentSummary {
        private String title;
        private String summary;
        private List<String> keyTopics;
        private String documentCategory;
        private int importanceLevel;
    }

    /**
     * 情感分析结果 POJO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SentimentResult {
        private String sentiment;
        private double confidence;
        private List<String> emotionKeywords;
        private String briefAnalysis;
    }

    private final DocumentSummaryExtractor summaryExtractor;
    private final SentimentAnalyzer sentimentAnalyzer;

    public StructuredExtractionService(dev.langchain4j.model.chat.ChatLanguageModel chatModel) {
        this.summaryExtractor = AiServices.create(DocumentSummaryExtractor.class, chatModel);
        this.sentimentAnalyzer = AiServices.create(SentimentAnalyzer.class, chatModel);
        log.info("StructuredExtractionService initialized with AiServices");
    }

    public DocumentSummary extractSummary(String content) {
        log.info("Extracting structured summary, content length: {}", content.length());
        return summaryExtractor.extract(content);
    }

    public SentimentResult analyzeSentiment(String content) {
        log.info("Analyzing sentiment, content length: {}", content.length());
        return sentimentAnalyzer.analyze(content);
    }
}
