package com.cgad.platform.controller;

import com.cgad.platform.model.dto.ChatMessage;
import com.cgad.platform.model.dto.ChatRequest;
import com.cgad.platform.service.StreamingChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class StreamingChatController {

    private final StreamingChatService streamingChatService;
    private final ExecutorService sseExecutor = Executors.newCachedThreadPool();

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(
            @RequestParam String message,
            @RequestParam(required = false) String systemPrompt) {

        log.info("SSE stream chat request: {}", message.substring(0, Math.min(message.length(), 50)));
        return streamingChatService.streamChat(message, systemPrompt, null);
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> streamChatPost(@RequestBody ChatRequest request) {
        log.info("SSE stream chat POST request, history: {}",
                request.getMessages() != null ? request.getMessages().size() : 0);

        List<dev.langchain4j.data.message.ChatMessage> history = new ArrayList<>();
        if (request.getMessages() != null) {
            for (ChatMessage msg : request.getMessages()) {
                switch (msg.getRole().toLowerCase()) {
                    case "user" -> history.add(dev.langchain4j.data.message.UserMessage.from(msg.getContent()));
                    case "assistant" -> history.add(dev.langchain4j.data.message.AiMessage.from(msg.getContent()));
                }
            }
        }

        SseEmitter emitter = streamingChatService.streamChat(
                request.getUserMessage(), request.getSystemPrompt(), history);

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .header("Cache-Control", "no-cache")
                .header("X-Accel-Buffering", "no")
                .body(emitter);
    }

    @PostMapping(value = "/stream/rag", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> streamChatWithRag(@RequestBody java.util.Map<String, String> body) {
        String question = body.get("question");
        if (question == null || question.isBlank()) {
            SseEmitter emitter = new SseEmitter();
            sseExecutor.execute(() -> {
                try {
                    emitter.send(SseEmitter.event().name("error").data("问题不能为空"));
                    emitter.complete();
                } catch (Exception ignored) {}
            });
            return ResponseEntity.badRequest().contentType(MediaType.TEXT_EVENT_STREAM).body(emitter);
        }

        log.info("SSE RAG stream chat request: {}", question.substring(0, Math.min(question.length(), 50)));
        SseEmitter emitter = streamingChatService.streamChatWithRag(question);

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .header("Cache-Control", "no-cache")
                .header("X-Accel-Buffering", "no")
                .body(emitter);
    }
}
