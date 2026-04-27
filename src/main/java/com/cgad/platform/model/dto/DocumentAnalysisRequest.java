package com.cgad.platform.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DocumentAnalysisRequest {

    @NotBlank(message = "文档内容不能为空")
    private String content;

    private String documentType;

    private boolean extractKeyClauses = true;

    private boolean generateSummary = true;
}
