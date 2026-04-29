package com.cgad.platform.controller;

import com.cgad.platform.model.dto.ApiResponse;
import com.cgad.platform.model.dto.ChatRequest;
import com.cgad.platform.model.dto.ChatResult;
import com.cgad.platform.service.ChatAssistantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/chat")
public class ChatAssistantController {

    private final ChatAssistantService chatAssistantService;

    public ChatAssistantController(ChatAssistantService chatAssistantService) {
        this.chatAssistantService = chatAssistantService;
    }

    @PostMapping
    public ApiResponse<ChatResult> chat(@RequestBody ChatRequest request) {
        log.info("Received chat request, history size: {}, userMessage: {}",
                request.getMessages() != null ? request.getMessages().size() : 0,
                request.getUserMessage().substring(0, Math.min(request.getUserMessage().length(), 50)));
        ChatResult result = chatAssistantService.chat(request);
        return ApiResponse.success(result);
    }
}
