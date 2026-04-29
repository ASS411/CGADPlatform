package com.cgad.platform.controller;

import com.cgad.platform.model.dto.ApiResponse;
import com.cgad.platform.model.dto.TranslationRequest;
import com.cgad.platform.model.dto.TranslationResult;
import com.cgad.platform.service.TranslationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 多语言翻译与润色控制器
 *
 * 提供两个端点：
 *   - POST /api/translation/translate  —— 标准翻译
 *   - POST /api/translation/polish     —— 翻译 + 润色
 *
 * 为什么翻译和润色分开两个端点？
 *   - 翻译和润色的提示词不同，润色模式会额外要求模型优化文本
 *   - 分开端点使 API 语义更清晰，前端调用更简单
 *   - polish 端点内部复用 translate 方法，只是将 polish 标志设为 true
 */
@Slf4j
@RestController
@RequestMapping("/api/translation")
public class TranslationController {

    private final TranslationService translationService;

    public TranslationController(TranslationService translationService) {
        this.translationService = translationService;
    }

    /**
     * 标准翻译接口
     *
     * 请求示例：
     *   POST /api/translation/translate
     *   { "sourceText": "Hello", "sourceLanguage": "en", "targetLanguage": "zh", "domain": "跨境电商" }
     */
    @PostMapping("/translate")
    public ApiResponse<TranslationResult> translate(
            @Valid @RequestBody TranslationRequest request) {
        log.info("Received translation request: {} -> {}",
                request.getSourceLanguage(), request.getTargetLanguage());
        TranslationResult result = translationService.translate(request);
        return ApiResponse.success(result);
    }

    /**
     * 翻译 + 润色接口
     *
     * 【知识点】API 设计技巧：
     *   - 在请求进入 Service 之前修改 request 的 polish 字段
     *   - 这样 Service 层只需检查 polish 标志，无需关心是哪个端点调用的
     *   - 体现了"控制器负责请求适配，服务层负责业务逻辑"的分层原则
     */
    @PostMapping("/polish")
    public ApiResponse<TranslationResult> polish(
            @Valid @RequestBody TranslationRequest request) {
        // 强制开启润色模式
        request.setPolish(true);
        log.info("Received polish request: {} -> {}",
                request.getSourceLanguage(), request.getTargetLanguage());
        TranslationResult result = translationService.translate(request);
        return ApiResponse.success(result);
    }
}
