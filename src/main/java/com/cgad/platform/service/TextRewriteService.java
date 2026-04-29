package com.cgad.platform.service;

import com.cgad.platform.model.dto.TextRewriteRequest;
import com.cgad.platform.model.dto.TextRewriteResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * AI 文本改写服务
 *
 * 【知识点】文本改写 vs 翻译 vs 润色的区别：
 *   - 翻译：改变语言（中文→英文）
 *   - 润色：保持原意，优化表达（消除语病、提升流畅度）
 *   - 改写：可以改变表达方式、篇幅、风格，但保留核心信息
 *
 * 【知识点】改写风格的设计思路：
 *   - simplify：去繁就简，适合快速阅读
 *   - expand：补充细节，适合深度阅读
 *   - formal：正式书面语，适合公文/报告
 *   - casual：轻松口语化，适合社交媒体
 *   - creative：创意表达，适合营销文案
 */
@Slf4j
@Service
public class TextRewriteService {

    private final ChatLanguageModel chatModel;
    private final ObjectMapper objectMapper;

    public TextRewriteService(ChatLanguageModel chatModel, ObjectMapper objectMapper) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
    }

    public TextRewriteResult rewrite(TextRewriteRequest request) {
        String prompt = buildPrompt(request);
        log.info("Sending text rewrite request, style: {}", request.getStyle());

        String rawResponse = chatModel.chat(prompt);
        log.debug("Raw rewrite response: {}", rawResponse);

        return parseResult(rawResponse);
    }

    private String buildPrompt(TextRewriteRequest request) {
        StringBuilder sb = new StringBuilder();

        sb.append("你是一个专业的文本改写专家。\n\n");

        sb.append("## 任务\n");
        sb.append("请按照指定风格对以下文本进行改写，保留核心信息但改变表达方式。\n\n");

        sb.append("## 改写风格\n");
        sb.append(getStyleDescription(request.getStyle())).append("\n\n");

        sb.append("## 目标受众\n");
        sb.append(getAudienceDescription(request.getAudience())).append("\n\n");

        if (request.getAdditionalRequirements() != null && !request.getAdditionalRequirements().isBlank()) {
            sb.append("## 额外要求\n");
            sb.append(request.getAdditionalRequirements()).append("\n\n");
        }

        sb.append("## 输出格式\n");
        sb.append("请严格按照以下JSON格式输出，不要输出任何其他内容：\n");
        sb.append("```json\n");
        sb.append("{\n");
        sb.append("  \"rewrittenText\": \"改写后的文本\",\n");
        sb.append("  \"changeSummary\": \"一句话概括做了哪些修改\",\n");
        sb.append("  \"changePoints\": [\"修改要点1\", \"修改要点2\"],\n");
        sb.append("  \"qualityScore\": 85\n");
        sb.append("}\n");
        sb.append("```\n\n");

        sb.append("## 原文\n");
        sb.append(request.getSourceText());

        return sb.toString();
    }

    private String getStyleDescription(String style) {
        return switch (style) {
            case "simplify" -> "简化：去除冗余信息，用更简洁的语言表达，适合快速阅读";
            case "expand" -> "扩写：补充细节和论据，使内容更丰富充实";
            case "formal" -> "正式化：使用书面正式用语，适合公文、报告、学术论文";
            case "casual" -> "口语化：使用轻松自然的表达，适合社交媒体、博客";
            case "creative" -> "创意化：使用新颖独特的表达方式，适合营销文案、广告";
            default -> "简化：去除冗余信息，用更简洁的语言表达";
        };
    }

    private String getAudienceDescription(String audience) {
        return switch (audience) {
            case "professional" -> "专业人士：使用行业术语，假设读者有相关背景知识";
            case "student" -> "学生：通俗易懂，适合学习和理解";
            case "executive" -> "高管：突出重点和结论，简洁有力";
            default -> "大众读者：平衡专业性和可读性";
        };
    }

    private TextRewriteResult parseResult(String rawResponse) {
        try {
            String jsonStr = extractJson(rawResponse);
            return objectMapper.readValue(jsonStr, new TypeReference<TextRewriteResult>() {});
        } catch (Exception e) {
            log.error("Failed to parse rewrite result", e);
            return TextRewriteResult.builder()
                    .rewrittenText(rawResponse)
                    .changeSummary("解析失败，返回原始输出")
                    .qualityScore(50)
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
