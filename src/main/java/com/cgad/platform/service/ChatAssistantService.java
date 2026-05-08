package com.cgad.platform.service;

import com.cgad.platform.model.dto.ChatMessage;
import com.cgad.platform.model.dto.ChatRequest;
import com.cgad.platform.model.dto.ChatResult;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.util.StringUtils.hasText;

@Slf4j
@Service
public class ChatAssistantService {

    private static final String DEFAULT_SYSTEM_PROMPT = "你是 CGAD Platform 的 AI 助手，" +
            "擅长文档分析、翻译润色和知识问答。请用简洁专业的中文回答用户的问题。";

    private static final int MAX_HISTORY_MESSAGES = 20;

    private final ChatLanguageModel chatModel;
    private final ChatHistoryService chatHistoryService;

    @Value("${langchain4j.provider:dashscope}")
    private String provider;

    @Value("${langchain4j.dashscope.model-name:qwen-plus}")
    private String dashscopeModelName;

    @Value("${langchain4j.deepseek.model-name:deepseek-chat}")
    private String deepseekModelName;

    public ChatAssistantService(ChatLanguageModel chatModel,
                                ChatHistoryService chatHistoryService) {
        this.chatModel = chatModel;
        this.chatHistoryService = chatHistoryService;
    }

    public ChatResult chat(ChatRequest request) {
        validateRequest(request);

        List<dev.langchain4j.data.message.ChatMessage> messages = new ArrayList<>();

        String systemPrompt = hasText(request.getSystemPrompt())
                ? request.getSystemPrompt()
                : DEFAULT_SYSTEM_PROMPT;

        messages.add(SystemMessage.from(systemPrompt));

        addHistoryMessages(messages, request.getMessages());

        messages.add(UserMessage.from(request.getUserMessage()));

        log.info("Sending chat request with {} messages", messages.size());

        String reply = chatModel.chat(messages).aiMessage().text();

        int promptTokens = estimateTokens(messages);
        int completionTokens = estimateTokens(reply);
        int totalTokens = promptTokens + completionTokens;

        String modelName = getModelName();

        String sessionId = hasText(request.getSessionId())
                ? request.getSessionId()
                : generateSessionId();

        chatHistoryService.saveMessage(
                sessionId,
                "user",
                request.getUserMessage(),
                promptTokens,
                provider,
                modelName
        );

        chatHistoryService.saveMessage(
                sessionId,
                "assistant",
                reply,
                completionTokens,
                provider,
                modelName
        );

        chatHistoryService.recordTokenUsage(
                provider,
                modelName,
                "chat",
                promptTokens,
                completionTokens,
                sessionId
        );

        return ChatResult.builder()
                .sesssionId(sessionId)
                .reply(reply)
                .tokensUsed(totalTokens)
                .build();
    }

    private void addHistoryMessages(
            List<dev.langchain4j.data.message.ChatMessage> messages,
            List<com.cgad.platform.model.dto.ChatMessage> historyMessages
    ) {
        if (historyMessages == null || historyMessages.isEmpty()) {
            return;
        }

        int startIndex = Math.max(0, historyMessages.size() - MAX_HISTORY_MESSAGES);

        for (int i = startIndex; i < historyMessages.size(); i++) {
            com.cgad.platform.model.dto.ChatMessage msg = historyMessages.get(i);

            if (msg == null || !hasText(msg.getRole()) || !hasText(msg.getContent())) {
                continue;
            }

            switch (msg.getRole().toLowerCase()) {
                case "user" -> messages.add(UserMessage.from(msg.getContent()));
                case "assistant" -> messages.add(AiMessage.from(msg.getContent()));
                default -> log.warn("Unknown message role: {}", msg.getRole());
            }
        }
    }

    private void validateRequest(ChatRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求不能为空");
        }

        if (!hasText(request.getUserMessage())) {
            throw new IllegalArgumentException("用户消息不能为空");
        }
    }

    private String getModelName() {
        if ("deepseek".equalsIgnoreCase(provider)) {
            return deepseekModelName;
        }
        return dashscopeModelName;
    }

    private String generateSessionId() {
        return "chat-" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12);
    }

    private int estimateTokens(List<dev.langchain4j.data.message.ChatMessage> messages) {
        int totalChars = 0;

        for (dev.langchain4j.data.message.ChatMessage msg : messages) {
            totalChars += msg.text().length();
        }

        return estimateTokens(totalChars);
    }

    private int estimateTokens(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }

        return estimateTokens(text.length());
    }

    private int estimateTokens(int charLength) {
        return (int) Math.ceil(charLength * 0.7);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}