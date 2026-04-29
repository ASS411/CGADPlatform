package com.cgad.platform.service;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;

/**
 * SSE 流式对话服务
 *
 * SSE（Server-Sent Events）打字机效果：
 *   - LLM 逐 token 生成，服务端逐个推送
 *   - SseEmitter 保持 HTTP 连接，通过 send() 推送数据
 *   - 前端通过 fetch + ReadableStream 实时接收
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StreamingChatService {

    private final StreamingChatLanguageModel streamingChatModel;
    private final RagService ragService;

    private static final String DEFAULT_SYSTEM_PROMPT = "你是 CGAD Platform 的 AI 助手，" +
            "擅长文档分析、翻译润色和知识问答。请用简洁专业的中文回答用户的问题。";

    public SseEmitter streamChat(String userMessage, String systemPrompt, List<dev.langchain4j.data.message.ChatMessage> history) {
        SseEmitter emitter = new SseEmitter(120_000L);

        List<dev.langchain4j.data.message.ChatMessage> messages = new ArrayList<>();
        String sp = (systemPrompt != null && !systemPrompt.isBlank()) ? systemPrompt : DEFAULT_SYSTEM_PROMPT;
        messages.add(SystemMessage.from(sp));
        if (history != null) {
            messages.addAll(history);
        }
        messages.add(UserMessage.from(userMessage));

        StringBuilder fullResponse = new StringBuilder();

        streamingChatModel.generate(messages, new StreamingResponseHandler<AiMessage>() {
            @Override
            public void onNext(String token) {
                try {
                    fullResponse.append(token);
                    emitter.send(SseEmitter.event()
                            .name("message")
                            .data(token));
                } catch (Exception e) {
                    log.error("Error sending SSE partial response", e);
                    emitter.completeWithError(e);
                }
            }

            @Override
            public void onComplete(Response<AiMessage> response) {
                try {
                    int estimatedTokens = (int) ((userMessage.length() + fullResponse.length()) * 0.7);
                    emitter.send(SseEmitter.event()
                            .name("done")
                            .data("{\"tokens\":" + estimatedTokens + "}"));
                    emitter.complete();
                    log.info("SSE stream completed, response length: {}", fullResponse.length());
                } catch (Exception e) {
                    log.error("Error completing SSE stream", e);
                    emitter.completeWithError(e);
                }
            }

            @Override
            public void onError(Throwable error) {
                log.error("SSE stream error", error);
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(error.getMessage()));
                } catch (Exception ignored) {
                }
                emitter.completeWithError(error);
            }
        });

        return emitter;
    }

    public SseEmitter streamChatWithRag(String userMessage) {
        SseEmitter emitter = new SseEmitter(120_000L);

        List<dev.langchain4j.data.message.ChatMessage> messages = new ArrayList<>();

        var relevantMatches = ragService.retrieveRelevant(userMessage);
        if (!relevantMatches.isEmpty()) {
            String context = relevantMatches.stream()
                    .map(match -> match.embedded().text())
                    .reduce((a, b) -> a + "\n\n---\n\n" + b)
                    .orElse("");

            String ragSystemPrompt = """
                    你是一个专业的文档分析助手。请基于以下检索到的文档内容回答用户的问题。
                    
                    要求：
                    1. 只基于提供的文档内容回答，不要编造信息
                    2. 如果文档中没有相关信息，请明确说明
                    3. 引用文档内容时，尽量保持原文表述
                    4. 回答要简洁、准确、有条理
                    
                    --- 检索到的文档内容 ---
                    %s
                    --- 文档内容结束 ---
                    """.formatted(context);
            messages.add(SystemMessage.from(ragSystemPrompt));
        } else {
            messages.add(SystemMessage.from(DEFAULT_SYSTEM_PROMPT));
        }

        messages.add(UserMessage.from(userMessage));

        StringBuilder fullResponse = new StringBuilder();

        streamingChatModel.generate(messages, new StreamingResponseHandler<AiMessage>() {
            @Override
            public void onNext(String token) {
                try {
                    fullResponse.append(token);
                    emitter.send(SseEmitter.event()
                            .name("message")
                            .data(token));
                } catch (Exception e) {
                    log.error("Error sending SSE RAG partial response", e);
                    emitter.completeWithError(e);
                }
            }

            @Override
            public void onComplete(Response<AiMessage> response) {
                try {
                    int estimatedTokens = (int) ((userMessage.length() + fullResponse.length()) * 0.7);
                    emitter.send(SseEmitter.event()
                            .name("done")
                            .data("{\"tokens\":" + estimatedTokens + ",\"ragSegments\":" + relevantMatches.size() + "}"));
                    emitter.complete();
                    log.info("SSE RAG stream completed, segments: {}", relevantMatches.size());
                } catch (Exception e) {
                    log.error("Error completing SSE RAG stream", e);
                    emitter.completeWithError(e);
                }
            }

            @Override
            public void onError(Throwable error) {
                log.error("SSE RAG stream error", error);
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(error.getMessage()));
                } catch (Exception ignored) {
                }
                emitter.completeWithError(error);
            }
        });

        return emitter;
    }
}
