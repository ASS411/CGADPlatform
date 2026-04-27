package com.cgad.platform.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TranslationRequest {

    @NotBlank(message = "原文内容不能为空")
    private String sourceText;

    private String sourceLanguage = "auto";

    @NotBlank(message = "目标语言不能为空")
    private String targetLanguage;

    private String domain;

    private String style;

    private boolean polish = false;
}
