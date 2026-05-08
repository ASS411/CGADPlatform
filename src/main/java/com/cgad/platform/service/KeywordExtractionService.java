package com.cgad.platform.service;

import com.cgad.platform.model.dto.KeywordExtractionRequest;
import com.cgad.platform.model.dto.KeywordExtractionResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 智能关键词提取服务
 *
 * 关键词提取的应用场景：
 *   - SEO 优化：自动生成网页 meta keywords
 *   - 内容标签：为文章自动打标签，便于分类检索
 *   - 知识图谱：提取实体构建知识关系
 *   - 情感分析：判断文本正负面倾向
 *
 * 为什么用大模型做关键词提取而非传统 NLP？
 *   - 传统方法（TF-IDF、TextRank）只能统计词频，不理解语义
 *   - 大模型能理解上下文，提取"隐含主题"而非高频词
 *   - 例如："降本增效" 可能只出现一次，但大模型知道它是核心关键词
 */
@Slf4j
@Service
public class KeywordExtractionService {

    private final ChatLanguageModel chatModel;
    private final ObjectMapper objectMapper;
    private final ChatHistoryService chatHistoryService;

    @Value("${langchain4j.provider:dashscope}")
    private String provider;

    @Value("${langchain4j.dashscope.model-name:qwen-plus}")
    private String dashscopeModelName;

    @Value("${langchain4j.deepseek.model-name:deepseek-chat}")
    private String deepseekModelName;

    public KeywordExtractionService(ChatLanguageModel chatModel, ObjectMapper objectMapper,
                                     ChatHistoryService chatHistoryService) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
        this.chatHistoryService = chatHistoryService;
    }

    public KeywordExtractionResult extract(KeywordExtractionRequest request) {
        String prompt = buildPrompt(request);
        log.info("Sending keyword extraction request, max keywords: {}", request.getMaxKeywords());

        String rawResponse = chatModel.chat(prompt);
        log.debug("Raw keyword extraction response: {}", rawResponse);

        int promptTokens = (int) (prompt.length() * 0.7);
        int completionTokens = (int) (rawResponse.length() * 0.7);
        String modelName = "deepseek".equalsIgnoreCase(provider) ? deepseekModelName : dashscopeModelName;
        chatHistoryService.recordTokenUsage(provider, modelName, "keyword_extraction",
                promptTokens, completionTokens, null);

        return parseResult(rawResponse);
    }

    private String buildPrompt(KeywordExtractionRequest request) {
        StringBuilder sb = new StringBuilder();

        sb.append("你是一个专业的文本分析专家，擅长从文本中提取关键信息。\n\n");

        sb.append("## 任务\n");
        sb.append("请从以下文本中提取最多 ").append(request.getMaxKeywords()).append(" 个关键词，");
        sb.append("并分析文本的主题和情感倾向。\n\n");

        if (request.getDomain() != null && !request.getDomain().isBlank()) {
            sb.append("## 领域提示\n");
            sb.append("该文本属于").append(request.getDomain()).append("领域，请优先提取该领域的专业术语。\n\n");
        }

        sb.append("## 输出格式\n");
        sb.append("请严格按照以下JSON格式输出，不要输出任何其他内容：\n");
        sb.append("```json\n");
        sb.append("{\n");
        sb.append("  \"keywords\": [\n");
        sb.append("    {\"keyword\": \"关键词\", \"relevance\": 0.95, \"category\": \"术语\"},\n");
        sb.append("    {\"keyword\": \"关键词2\", \"relevance\": 0.8, \"category\": \"实体\"}\n");
        sb.append("  ],\n");
        sb.append("  \"topicSummary\": \"一句话概括文本主题\",\n");
        sb.append("  \"sentiment\": \"正面/中性/负面\"\n");
        sb.append("}\n");
        sb.append("```\n");
        sb.append("其中 category 可选值：术语、实体、主题、动作、属性\n\n");

        sb.append("## 待分析文本\n");
        sb.append(request.getText());

        return sb.toString();
    }

    private KeywordExtractionResult parseResult(String rawResponse) {
        try {
            String jsonStr = extractJson(rawResponse);
            return objectMapper.readValue(jsonStr, new TypeReference<KeywordExtractionResult>() {});
        } catch (Exception e) {
            log.error("Failed to parse keyword extraction result", e);
            return KeywordExtractionResult.builder()
                    .topicSummary("解析失败")
                    .sentiment("未知")
                    .build();
        }
    }

    private String extractJson(String text) {
        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }
}
