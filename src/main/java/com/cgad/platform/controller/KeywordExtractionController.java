package com.cgad.platform.controller;

import com.cgad.platform.model.dto.ApiResponse;
import com.cgad.platform.model.dto.KeywordExtractionRequest;
import com.cgad.platform.model.dto.KeywordExtractionResult;
import com.cgad.platform.service.KeywordExtractionService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 智能关键词提取控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/keyword")
public class KeywordExtractionController {

    private final KeywordExtractionService keywordExtractionService;

    public KeywordExtractionController(KeywordExtractionService keywordExtractionService) {
        this.keywordExtractionService = keywordExtractionService;
    }

    @PostMapping("/extract")
    public ApiResponse<KeywordExtractionResult> extract(
            @Valid @RequestBody KeywordExtractionRequest request) {
        log.info("Received keyword extraction request");
        KeywordExtractionResult result = keywordExtractionService.extract(request);
        return ApiResponse.success(result);
    }
}
