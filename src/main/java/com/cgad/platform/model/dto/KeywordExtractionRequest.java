package com.cgad.platform.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 关键词提取请求 DTO
 */
@Data
public class KeywordExtractionRequest {

    @NotBlank(message = "文本内容不能为空")
    private String text;

    /** 期望提取的关键词数量，默认 10 */
    private int maxKeywords = 10;

    /** 文本领域提示（如：科技、金融、法律），帮助模型提取更精准的术语 */
    private String domain;
}
