package com.cgad.platform.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 对话结果 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResult {

    /** AI 助手的回复内容 */
    private String reply;

    /** 本轮对话消耗的 token 数（估算） */
    private int tokensUsed;
}
