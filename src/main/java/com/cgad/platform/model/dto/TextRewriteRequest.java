package com.cgad.platform.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 文本改写请求 DTO
 */
@Data
public class TextRewriteRequest {

    @NotBlank(message = "原文内容不能为空")
    private String sourceText;

    /** 改写风格：simplify(简化) / expand(扩写) / formal(正式) / casual(口语) / creative(创意) */
    private String style = "simplify";

    /** 目标受众：general(大众) / professional(专业) / student(学生) / executive(高管) */
    private String audience = "general";

    /** 改写要求补充说明 */
    private String additionalRequirements;
}
