package com.cgad.platform.controller;

import com.cgad.platform.model.dto.ApiResponse;
import com.cgad.platform.model.dto.TranslationRequest;
import com.cgad.platform.model.dto.TranslationResult;
import com.cgad.platform.service.TranslationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/translation")
public class TranslationController {

    private final TranslationService translationService;

    public TranslationController(TranslationService translationService) {
        this.translationService = translationService;
    }

    @PostMapping("/translate")
    public ApiResponse<TranslationResult> translate(
            @Valid @RequestBody TranslationRequest request) {
        log.info("Received translation request: {} -> {}",
                request.getSourceLanguage(), request.getTargetLanguage());
        TranslationResult result = translationService.translate(request);
        return ApiResponse.success(result);
    }

    @PostMapping("/polish")
    public ApiResponse<TranslationResult> polish(
            @Valid @RequestBody TranslationRequest request) {
        request.setPolish(true);
        log.info("Received polish request: {} -> {}",
                request.getSourceLanguage(), request.getTargetLanguage());
        TranslationResult result = translationService.translate(request);
        return ApiResponse.success(result);
    }
}
