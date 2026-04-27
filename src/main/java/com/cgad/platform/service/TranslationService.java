package com.cgad.platform.service;

import com.cgad.platform.model.dto.TranslationRequest;
import com.cgad.platform.model.dto.TranslationResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TranslationService {

    private final ChatLanguageModel chatModel;
    private final ObjectMapper objectMapper;

    public TranslationService(ChatLanguageModel chatModel, ObjectMapper objectMapper) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
    }

    public TranslationResult translate(TranslationRequest request) {
        String prompt = buildTranslationPrompt(request);
        log.info("Sending translation request, source language: {}, target language: {}",
                request.getSourceLanguage(), request.getTargetLanguage());

        String rawResponse = chatModel.chat(prompt);
        log.debug("Raw model response: {}", rawResponse);

        return parseTranslationResult(rawResponse, request);
    }

    private String buildTranslationPrompt(TranslationRequest request) {
        StringBuilder sb = new StringBuilder();

        sb.append("你是一个专业的多语言翻译专家");

        if (request.getDomain() != null && !request.getDomain().isBlank()) {
            sb.append("，特别擅长").append(request.getDomain()).append("领域的专业术语翻译");
        }

        sb.append("。\n\n");

        sb.append("## 任务\n");
        if (request.isPolish()) {
            sb.append("请对以下文本进行翻译并进行润色，使其在目标语言中更加地道、专业。\n");
        } else {
            sb.append("请将以下文本翻译为目标语言。\n");
        }

        sb.append("\n## 翻译要求\n");
        sb.append("- 源语言：").append("auto".equals(request.getSourceLanguage()) ? "自动检测" : request.getSourceLanguage()).append("\n");
        sb.append("- 目标语言：").append(request.getTargetLanguage()).append("\n");

        if (request.getDomain() != null && !request.getDomain().isBlank()) {
            sb.append("- 专业领域：").append(request.getDomain()).append("\n");
            sb.append("- 请确保专业术语的翻译准确且符合该领域的行业惯例\n");
        }

        if (request.getStyle() != null && !request.getStyle().isBlank()) {
            sb.append("- 输出风格：").append(request.getStyle()).append("\n");
        }

        if (request.isPolish()) {
            sb.append("- 需要对翻译结果进行润色，使其更加流畅自然\n");
        }

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

        sb.append("## 原文\n");
        sb.append(request.getSourceText());

        return sb.toString();
    }

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
