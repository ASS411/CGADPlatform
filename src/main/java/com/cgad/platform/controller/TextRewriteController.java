package com.cgad.platform.controller;

import com.cgad.platform.model.dto.ApiResponse;
import com.cgad.platform.model.dto.TextRewriteRequest;
import com.cgad.platform.model.dto.TextRewriteResult;
import com.cgad.platform.service.TextRewriteService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * AI 文本改写控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/rewrite")
public class TextRewriteController {

    private final TextRewriteService textRewriteService;

    public TextRewriteController(TextRewriteService textRewriteService) {
        this.textRewriteService = textRewriteService;
    }

    @PostMapping
    public ApiResponse<TextRewriteResult> rewrite(
            @Valid @RequestBody TextRewriteRequest request) {
        log.info("Received text rewrite request, style: {}", request.getStyle());
        TextRewriteResult result = textRewriteService.rewrite(request);
        return ApiResponse.success(result);
    }
}
