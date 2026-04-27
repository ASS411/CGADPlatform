package com.cgad.platform.service;

import com.cgad.platform.model.dto.DocumentAnalysisRequest;
import com.cgad.platform.model.dto.DocumentAnalysisResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DocumentAnalyzerService {

    private final ChatLanguageModel chatModel;
    private final ObjectMapper objectMapper;

    public DocumentAnalyzerService(ChatLanguageModel chatModel, ObjectMapper objectMapper) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
    }

    public DocumentAnalysisResult analyzeDocument(DocumentAnalysisRequest request) {
        String prompt = buildAnalysisPrompt(request);
        log.info("Sending document analysis request, content length: {}", request.getContent().length());

        String rawResponse = chatModel.chat(prompt);
        log.debug("Raw model response: {}", rawResponse);

        return parseStructuredOutput(rawResponse);
    }

    private String buildAnalysisPrompt(DocumentAnalysisRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个专业的文档分析助手。请对以下文档进行深度分析，并严格按照JSON格式输出结果。\n\n");

        sb.append("## 分析要求\n");
        if (request.isGenerateSummary()) {
            sb.append("1. 生成文档摘要（summary字段）：用200字以内概括文档核心内容\n");
        }
        if (request.isExtractKeyClauses()) {
            sb.append("2. 提取关键条款（keyClauses数组）：提取文档中的关键条款，每条包含：\n");
            sb.append("   - clauseType: 条款类型（如：金额/日期/违约责任/保密条款/知识产权/付款条件等）\n");
            sb.append("   - clauseContent: 条款原文内容\n");
            sb.append("   - clauseValue: 条款的具体数值或关键信息\n");
            sb.append("   - location: 条款在文档中的位置描述\n");
        }
        sb.append("3. 提取关键要点（keyPoints数组）：列出3-5个文档的核心要点\n");
        sb.append("4. 风险评估（riskAssessment字段）：识别文档中可能存在的风险点\n");

        if (request.getDocumentType() != null && !request.getDocumentType().isBlank()) {
            sb.append("\n文档类型提示：").append(request.getDocumentType()).append("\n");
        }

        sb.append("\n## 输出格式\n");
        sb.append("请严格按照以下JSON格式输出，不要输出任何其他内容：\n");
        sb.append("```json\n");
        sb.append("{\n");
        sb.append("  \"summary\": \"文档摘要\",\n");
        sb.append("  \"documentType\": \"识别出的文档类型\",\n");
        sb.append("  \"keyClauses\": [\n");
        sb.append("    {\n");
        sb.append("      \"clauseType\": \"条款类型\",\n");
        sb.append("      \"clauseContent\": \"条款内容\",\n");
        sb.append("      \"clauseValue\": \"具体数值\",\n");
        sb.append("      \"location\": \"位置描述\"\n");
        sb.append("    }\n");
        sb.append("  ],\n");
        sb.append("  \"keyPoints\": [\"要点1\", \"要点2\", \"要点3\"],\n");
        sb.append("  \"riskAssessment\": \"风险评估\"\n");
        sb.append("}\n");
        sb.append("```\n\n");

        sb.append("## 文档内容\n");
        sb.append(request.getContent());

        return sb.toString();
    }

    private DocumentAnalysisResult parseStructuredOutput(String rawResponse) {
        try {
            String jsonStr = extractJson(rawResponse);
            return objectMapper.readValue(jsonStr, DocumentAnalysisResult.class);
        } catch (Exception e) {
            log.error("Failed to parse structured output, attempting fallback parsing", e);
            return DocumentAnalysisResult.builder()
                    .summary(rawResponse)
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
