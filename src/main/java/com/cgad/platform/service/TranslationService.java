package com.cgad.platform.service;

import com.cgad.platform.model.dto.TranslationRequest;
import com.cgad.platform.model.dto.TranslationResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 多语言翻译与润色服务 —— 本项目的核心功能之二
 *
 * 【核心能力】通过提示词工程（Prompt Engineering）定制特定风格的翻译输出：
 *   - 支持指定专业领域（如跨境电商），确保术语翻译准确
 *   - 支持指定输出风格（正式/口语/学术等）
 *   - 支持润色模式，在翻译基础上优化文本质量
 *
 * 【关键设计】领域定制翻译 vs 通用翻译：
 *   - 通用翻译：直接翻译，不考虑专业术语
 *   - 领域定制翻译：在提示词中指定领域，模型会使用该领域的标准术语
 *   - 例如："SKU" 在通用翻译中可能被翻译为"库存单位"，但在跨境电商领域应保留为 "SKU"
 *
 * 【知识点】Prompt Engineering 的核心原则：
 *   - 明确性：告诉模型"做什么"而非"不做什么"
 *   - 具体性：给出具体的输出格式和示例
 *   - 上下文：提供足够的背景信息（领域、风格等）
 */
@Slf4j
@Service
public class TranslationService {

    private final ChatLanguageModel chatModel;
    private final ObjectMapper objectMapper;

    public TranslationService(ChatLanguageModel chatModel, ObjectMapper objectMapper) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
    }

    /**
     * 翻译文本 —— 主入口方法
     *
     * 处理流程与文档分析类似：
     *   1. 构建翻译提示词（根据领域、风格、是否润色动态生成）
     *   2. 调用大模型
     *   3. 解析结构化输出
     */
    public TranslationResult translate(TranslationRequest request) {
        String prompt = buildTranslationPrompt(request);
        log.info("Sending translation request, source language: {}, target language: {}",
                request.getSourceLanguage(), request.getTargetLanguage());

        String rawResponse = chatModel.chat(prompt);
        log.debug("Raw model response: {}", rawResponse);

        return parseTranslationResult(rawResponse, request);
    }

    /**
     * 构建翻译提示词 —— Prompt Engineering 的核心
     *
     * 【知识点】动态提示词构建：
     *   - 根据用户的选择（领域、风格、润色模式）动态调整提示词内容
     *   - 这比固定提示词更灵活，能适应不同场景的翻译需求
     *
     * 提示词结构：
     *   1. 角色设定 + 领域专长（动态）
     *   2. 任务描述（翻译 or 翻译+润色）
     *   3. 翻译要求（源语言、目标语言、领域、风格）
     *   4. 输出格式（JSON Schema）
     *   5. 原文内容
     */
    private String buildTranslationPrompt(TranslationRequest request) {
        StringBuilder sb = new StringBuilder();

        // 角色设定：基础角色 + 领域专长
        sb.append("你是一个专业的多语言翻译专家");

        // 【关键】如果指定了领域，在角色设定中强调领域专长
        // 这会让模型更倾向于使用该领域的专业术语
        if (request.getDomain() != null && !request.getDomain().isBlank()) {
            sb.append("，特别擅长").append(request.getDomain()).append("领域的专业术语翻译");
        }

        sb.append("。\n\n");

        // 任务描述：区分"翻译"和"翻译+润色"两种模式
        sb.append("## 任务\n");
        if (request.isPolish()) {
            sb.append("请对以下文本进行翻译并进行润色，使其在目标语言中更加地道、专业。\n");
        } else {
            sb.append("请将以下文本翻译为目标语言。\n");
        }

        // 翻译要求：精确控制翻译行为
        sb.append("\n## 翻译要求\n");
        sb.append("- 源语言：").append("auto".equals(request.getSourceLanguage()) ? "自动检测" : request.getSourceLanguage()).append("\n");
        sb.append("- 目标语言：").append(request.getTargetLanguage()).append("\n");

        // 领域要求：确保术语准确性
        if (request.getDomain() != null && !request.getDomain().isBlank()) {
            sb.append("- 专业领域：").append(request.getDomain()).append("\n");
            sb.append("- 请确保专业术语的翻译准确且符合该领域的行业惯例\n");
        }

        // 风格要求：控制输出的语气和正式程度
        if (request.getStyle() != null && !request.getStyle().isBlank()) {
            sb.append("- 输出风格：").append(request.getStyle()).append("\n");
        }

        // 润色要求
        if (request.isPolish()) {
            sb.append("- 需要对翻译结果进行润色，使其更加流畅自然\n");
        }

        // 输出格式：结构化 JSON，包含术语注释和置信度
        sb.append("\n## 输出格式\n");
        sb.append("请严格按照以下JSON格式输出，不要输出任何其他内容：\n");
        sb.append("```json\n");
        sb.append("{\n");
        sb.append("  \"translatedText\": \"翻译后的文本\",\n");
        sb.append("  \"detectedSourceLanguage\": \"检测到的源语言\",\n");
        sb.append("  \"glossaryNotes\": [\"术语说明1\", \"术语说明2\"],\n");
        sb.append("  \"confidence\": 0.95\n");
        sb.append("}\n");
        sb.append("```\n\n");

        // 原文内容
        sb.append("## 原文\n");
        sb.append(request.getSourceText());

        return sb.toString();
    }

    /**
     * 解析翻译结果
     *
     * 降级策略：如果 JSON 解析失败，将原始输出作为翻译文本返回
     * 并设置较低的置信度（0.5），表示结果可能不完整
     */
    private TranslationResult parseTranslationResult(String rawResponse, TranslationRequest request) {
        try {
            String jsonStr = extractJson(rawResponse);
            return objectMapper.readValue(jsonStr, TranslationResult.class);
        } catch (Exception e) {
            log.error("Failed to parse translation result, using fallback", e);
            return TranslationResult.builder()
                    .translatedText(rawResponse)
                    .detectedSourceLanguage(request.getSourceLanguage())
                    .confidence(0.5)
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
