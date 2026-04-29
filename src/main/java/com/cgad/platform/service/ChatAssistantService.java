package com.cgad.platform.service;

import com.cgad.platform.model.dto.ChatMessage;
import com.cgad.platform.model.dto.ChatRequest;
import com.cgad.platform.model.dto.ChatResult;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ChatAssistantService {

    private static final String DEFAULT_SYSTEM_PROMPT = "你是 CGAD Platform 的 AI 助手，" +
            "擅长文档分析、翻译润色和知识问答。请用简洁专业的中文回答用户的问题。";

    private final ChatLanguageModel chatModel;

    public ChatAssistantService(ChatLanguageModel chatModel) {
        this.chatModel = chatModel;
    }

    public ChatResult chat(ChatRequest request) {
        List<dev.langchain4j.data.message.ChatMessage> messages = new ArrayList<>();

        String systemPrompt = (request.getSystemPrompt() != null && !request.getSystemPrompt().isBlank())
                ? request.getSystemPrompt()
                : DEFAULT_SYSTEM_PROMPT;
        messages.add(SystemMessage.from(systemPrompt));

        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            for (ChatMessage msg : request.getMessages()) {
                switch (msg.getRole().toLowerCase()) {
                    case "user" -> messages.add(UserMessage.from(msg.getContent()));
                    case "assistant" -> messages.add(AiMessage.from(msg.getContent()));
                    default -> log.warn("Unknown message role: {}", msg.getRole());
                }
            }
        }

        messages.add(UserMessage.from(request.getUserMessage()));

        log.info("Sending chat request with {} history messages", messages.size());

        String reply = chatModel.chat(messages).aiMessage().text();
        log.info("Chat reply length: {} chars", reply.length());

        int estimatedTokens = estimateTokens(messages, reply);

        return ChatResult.builder()
                .reply(reply)
                .tokensUsed(estimatedTokens)
                .build();
    }

    private int estimateTokens(List<dev.langchain4j.data.message.ChatMessage> messages, String reply) {
        int totalChars = 0;
        for (dev.langchain4j.data.message.ChatMessage msg : messages) {
            totalChars += msg.text().length();
        }
        totalChars += reply.length();
        return (int) (totalChars * 0.7);
    }
}
