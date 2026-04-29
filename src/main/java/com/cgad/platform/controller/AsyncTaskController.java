package com.cgad.platform.controller;

import com.cgad.platform.model.dto.ApiResponse;
import com.cgad.platform.service.AsyncDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 异步任务控制器
 *
 * 异步任务 API 设计模式：
 *   - POST /async/submit：提交任务，立即返回 taskId
 *   - GET /async/status/{taskId}：查询任务状态和进度
 *   - 前端轮询 status 接口，直到任务完成
 *
 * 为什么不用 WebSocket 推送进度？
 *   - 轮询实现更简单，兼容性更好
 *   - 文档分析场景下轮询频率不高（1-2秒一次）
 *   - SSE 也可以推送进度，但需要额外管理连接
 */
@Slf4j
@RestController
@RequestMapping("/api/async")
@RequiredArgsConstructor
public class AsyncTaskController {

    private final AsyncDocumentService asyncDocumentService;

    @PostMapping("/submit/analysis")
    public ApiResponse<Map<String, String>> submitAnalysis(@RequestBody Map<String, String> body) {
        String content = body.get("content");
        String documentType = body.getOrDefault("documentType", "");

        if (content == null || content.isBlank()) {
            return ApiResponse.error("文档内容不能为空");
        }

        String taskId = asyncDocumentService.submitAnalysisTask(content, documentType);
        return ApiResponse.success(Map.of("taskId", taskId));
    }

    @PostMapping("/submit/ingestion")
    public ApiResponse<Map<String, String>> submitIngestion(@RequestBody Map<String, String> body) {
        String content = body.get("content");
        String sourceName = body.getOrDefault("sourceName", "unnamed");

        if (content == null || content.isBlank()) {
            return ApiResponse.error("文档内容不能为空");
        }

        String taskId = asyncDocumentService.submitIngestionTask(content, sourceName);
        return ApiResponse.success(Map.of("taskId", taskId));
    }

    @GetMapping("/status/{taskId}")
    public ApiResponse<Map<String, Object>> getTaskStatus(@PathVariable String taskId) {
        Map<String, Object> status = asyncDocumentService.getTaskStatus(taskId);
        return ApiResponse.success(status);
    }
}
