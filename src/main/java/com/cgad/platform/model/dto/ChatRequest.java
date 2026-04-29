package com.cgad.platform.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ChatRequest {

    private List<ChatMessage> messages;

    private String systemPrompt;

    @NotBlank(message = "消息内容不能为空")
    private String userMessage;
}
