package com.cgad.platform.service;

import com.cgad.platform.model.dto.DocumentAnalysisResult;
import com.cgad.platform.model.dto.KeywordExtractionResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 异步文档处理服务
 *
 * Spring @Async + Redis 实现异步任务管理：
 *   - @Async：方法在独立线程池中执行，不阻塞 HTTP 请求线程
 *   - Redis：存储任务状态，前端通过轮询获取进度
 *   - 工作流程：提交任务 → 返回 taskId → 后台处理 → Redis 更新状态 → 前端轮询
 *
 * 为什么用 Redis 而不是内存 Map？
 *   - Redis 支持持久化，服务重启不丢失任务状态
 *   - 支持分布式场景，多个服务实例可共享状态
 *   - 自带 TTL 过期机制，自动清理过期任务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncDocumentService {

    private final DocumentAnalyzerService documentAnalyzerService;
    private final KeywordExtractionService keywordExtractionService;
    private final DocumentIngestionService documentIngestionService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String TASK_KEY_PREFIX = "task:";
    private static final long TASK_TTL_HOURS = 2;

    public String submitAnalysisTask(String content, String documentType) {
        String taskId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        initTask(taskId, "DOCUMENT_ANALYSIS", content.length());
        processAnalysisAsync(taskId, content, documentType);
        return taskId;
    }

    public String submitIngestionTask(String content, String sourceName) {
        String taskId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        initTask(taskId, "DOCUMENT_INGESTION", content.length());
        processIngestionAsync(taskId, content, sourceName);
        return taskId;
    }

    public String submitKeywordTask(String text, int maxKeywords, String domain) {
        String taskId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        initTask(taskId, "KEYWORD_EXTRACTION", text.length());
        processKeywordAsync(taskId, text, maxKeywords, domain);
        return taskId;
    }

    public Map<String, Object> getTaskStatus(String taskId) {
        String key = TASK_KEY_PREFIX + taskId;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        if (entries.isEmpty()) {
            Map<String, Object> notFound = new HashMap<>();
            notFound.put("status", "NOT_FOUND");
            return notFound;
        }
        Map<String, Object> result = new HashMap<>();
        entries.forEach((k, v) -> result.put(k.toString(), v.toString()));
        return result;
    }

    @Async("documentTaskExecutor")
    public void processAnalysisAsync(String taskId, String content, String documentType) {
        try {
            updateTaskProgress(taskId, 20, "正在解析文档结构...");

            Thread.sleep(300);

            updateTaskProgress(taskId, 50, "正在提取关键信息...");

            var request = new com.cgad.platform.model.dto.DocumentAnalysisRequest();
            request.setContent(content);
            request.setDocumentType(documentType);
            DocumentAnalysisResult analysisResult = documentAnalyzerService.analyzeDocument(request);

            updateTaskProgress(taskId, 80, "正在生成分析报告...");

            String resultJson = objectMapper.writeValueAsString(analysisResult);
            completeTask(taskId, resultJson);

        } catch (Exception e) {
            log.error("Async analysis task failed: {}", taskId, e);
            failTask(taskId, e.getMessage());
        }
    }

    @Async("documentTaskExecutor")
    public void processIngestionAsync(String taskId, String content, String sourceName) {
        try {
            updateTaskProgress(taskId, 30, "正在切片文档...");

            Thread.sleep(200);

            updateTaskProgress(taskId, 60, "正在向量化并存储...");

            documentIngestionService.ingestText(content, sourceName);

            updateTaskProgress(taskId, 90, "文档摄入完成");
            completeTask(taskId, "{\"message\":\"文档已成功摄入向量数据库\",\"source\":\"" + sourceName + "\"}");

        } catch (Exception e) {
            log.error("Async ingestion task failed: {}", taskId, e);
            failTask(taskId, e.getMessage());
        }
    }

    @Async("documentTaskExecutor")
    public void processKeywordAsync(String taskId, String text, int maxKeywords, String domain) {
        try {
            updateTaskProgress(taskId, 40, "正在提取关键词...");

            var request = new com.cgad.platform.model.dto.KeywordExtractionRequest();
            request.setText(text);
            request.setMaxKeywords(maxKeywords);
            request.setDomain(domain);
            KeywordExtractionResult result = keywordExtractionService.extract(request);

            String resultJson = objectMapper.writeValueAsString(result);
            completeTask(taskId, resultJson);

        } catch (Exception e) {
            log.error("Async keyword task failed: {}", taskId, e);
            failTask(taskId, e.getMessage());
        }
    }

    private void initTask(String taskId, String type, int contentLength) {
        String key = TASK_KEY_PREFIX + taskId;
        Map<String, String> fields = new HashMap<>();
        fields.put("taskId", taskId);
        fields.put("type", type);
        fields.put("status", "PROCESSING");
        fields.put("progress", "0");
        fields.put("message", "任务已提交");
        fields.put("contentLength", String.valueOf(contentLength));
        fields.put("createTime", String.valueOf(System.currentTimeMillis()));
        redisTemplate.opsForHash().putAll(key, fields);
        redisTemplate.expire(key, TASK_TTL_HOURS, TimeUnit.HOURS);
        log.info("Task initialized: {} (type={})", taskId, type);
    }

    private void updateTaskProgress(String taskId, int progress, String message) {
        String key = TASK_KEY_PREFIX + taskId;
        redisTemplate.opsForHash().put(key, "progress", String.valueOf(progress));
        redisTemplate.opsForHash().put(key, "message", message);
        log.debug("Task progress: {} - {}%", taskId, progress);
    }

    private void completeTask(String taskId, String resultJson) {
        String key = TASK_KEY_PREFIX + taskId;
        redisTemplate.opsForHash().put(key, "status", "COMPLETED");
        redisTemplate.opsForHash().put(key, "progress", "100");
        redisTemplate.opsForHash().put(key, "message", "处理完成");
        redisTemplate.opsForHash().put(key, "result", resultJson);
        log.info("Task completed: {}", taskId);
    }

    private void failTask(String taskId, String error) {
        String key = TASK_KEY_PREFIX + taskId;
        redisTemplate.opsForHash().put(key, "status", "FAILED");
        redisTemplate.opsForHash().put(key, "message", "处理失败: " + error);
        log.error("Task failed: {} - {}", taskId, error);
    }
}
