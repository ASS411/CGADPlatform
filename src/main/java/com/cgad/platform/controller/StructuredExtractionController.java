package com.cgad.platform.controller;

import com.cgad.platform.model.dto.ApiResponse;
import com.cgad.platform.service.StructuredExtractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 结构化提取控制器
 *
 * AiServices + POJO 输出的 API 展示：
 *   - /extract/summary：提取文档结构化摘要
 *   - /extract/sentiment：分析文本情感
 *   - 返回的是标准 Java 对象，自动序列化为 JSON
 *   - 前端可直接用于可视化（如 ECharts 雷达图）
 */
@Slf4j
@RestController
@RequestMapping("/api/extract")
@RequiredArgsConstructor
public class StructuredExtractionController {

    private final StructuredExtractionService extractionService;

    @PostMapping("/summary")
    public ApiResponse<StructuredExtractionService.DocumentSummary> extractSummary(
            @RequestBody Map<String, String> body) {
        String content = body.get("content");
        if (content == null || content.isBlank()) {
            return ApiResponse.error("文档内容不能为空");
        }

        StructuredExtractionService.DocumentSummary summary = extractionService.extractSummary(content);
        return ApiResponse.success(summary);
    }

    @PostMapping("/sentiment")
    public ApiResponse<StructuredExtractionService.SentimentResult> analyzeSentiment(
            @RequestBody Map<String, String> body) {
        String content = body.get("content");
        if (content == null || content.isBlank()) {
            return ApiResponse.error("文本内容不能为空");
        }

        StructuredExtractionService.SentimentResult result = extractionService.analyzeSentiment(content);
        return ApiResponse.success(result);
    }
}
