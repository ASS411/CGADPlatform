package com.cgad.platform.controller;

import com.cgad.platform.model.dto.ApiResponse;
import com.cgad.platform.service.DocumentIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * RAG 文档管理控制器
 *
 * RAG（Retrieval Augmented Generation）的 API 设计：
 *   - /ingest：摄入文档到向量数据库（切片 → 向量化 → 存储）
 *   - /ask：基于已摄入的文档进行检索增强问答
 *   - 前端先摄入文档，再基于文档提问
 */
@Slf4j
@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
public class RagController {

    private final DocumentIngestionService ingestionService;
    private final com.cgad.platform.service.RagService ragService;

    @PostMapping("/ingest")
    public ApiResponse<String> ingestDocument(@RequestBody Map<String, String> body) {
        String content = body.get("content");
        String sourceName = body.getOrDefault("sourceName", "unnamed");

        if (content == null || content.isBlank()) {
            return ApiResponse.error("文档内容不能为空");
        }

        ingestionService.ingestText(content, sourceName);
        return ApiResponse.success("文档摄入成功，共处理 " + content.length() + " 字符");
    }

    @PostMapping("/ingest/file")
    public ApiResponse<String> ingestFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "sourceName", required = false) String sourceName) throws IOException {

        if (sourceName == null || sourceName.isBlank()) {
            sourceName = file.getOriginalFilename();
        }

        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        ingestionService.ingestText(content, sourceName);
        return ApiResponse.success("文件摄入成功：" + sourceName);
    }

    @PostMapping("/ask")
    public ApiResponse<String> askWithRag(@RequestBody Map<String, String> body) {
        String question = body.get("question");
        if (question == null || question.isBlank()) {
            return ApiResponse.error("问题不能为空");
        }

        String answer = ragService.askWithRag(question);
        return ApiResponse.success(answer);
    }
}
