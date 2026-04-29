package com.cgad.platform.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LangChain4j 配置类 —— 大模型接入的核心配置
 *
 * 统一接口，多模型适配：
 *   - ChatLanguageModel：同步聊天模型
 *   - StreamingChatLanguageModel：流式聊天模型（SSE 打字机效果）
 *   - EmbeddingModel：文本向量化模型（RAG 检索的基础）
 */
@Slf4j
@Configuration
public class LangChain4jConfig {

    @Value("${langchain4j.provider:dashscope}")
    private String provider;

    @Value("${langchain4j.dashscope.api-key}")
    private String dashscopeApiKey;

    @Value("${langchain4j.dashscope.model-name:qwen-plus}")
    private String dashscopeModelName;

    @Value("${langchain4j.dashscope.temperature:0.1}")
    private double dashscopeTemperature;

    @Value("${langchain4j.dashscope.max-tokens:4096}")
    private int dashscopeMaxTokens;

    @Value("${langchain4j.open-ai.api-key}")
    private String openAiApiKey;

    @Value("${langchain4j.open-ai.model-name:gpt-4o}")
    private String openAiModelName;

    @Value("${langchain4j.open-ai.base-url:https://api.openai.com/v1}")
    private String openAiBaseUrl;

    @Value("${langchain4j.open-ai.temperature:0.1}")
    private double openAiTemperature;

    @Value("${langchain4j.open-ai.max-tokens:4096}")
    private int openAiMaxTokens;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        if ("openai".equalsIgnoreCase(provider)) {
            return OpenAiChatModel.builder()
                    .apiKey(openAiApiKey)
                    .modelName(openAiModelName)
                    .baseUrl(openAiBaseUrl)
                    .temperature(openAiTemperature)
                    .maxTokens(openAiMaxTokens)
                    .build();
        }
        return QwenChatModel.builder()
                .apiKey(dashscopeApiKey)
                .modelName(dashscopeModelName)
                .temperature((float) dashscopeTemperature)
                .maxTokens(dashscopeMaxTokens)
                .build();
    }

    /**
     * 流式聊天模型 —— SSE 打字机效果的核心
     *
     * 与 ChatLanguageModel 的区别：
     *   - ChatLanguageModel.chat() 等待完整响应后返回
     *   - StreamingChatModel.chat() 逐 token 返回，实现打字机效果
     *   - 底层使用 SSE（Server-Sent Events）协议推送
     */
    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel() {
        if ("openai".equalsIgnoreCase(provider)) {
            return OpenAiStreamingChatModel.builder()
                    .apiKey(openAiApiKey)
                    .modelName(openAiModelName)
                    .baseUrl(openAiBaseUrl)
                    .temperature(openAiTemperature)
                    .maxTokens(openAiMaxTokens)
                    .build();
        }
        return QwenStreamingChatModel.builder()
                .apiKey(dashscopeApiKey)
                .modelName(dashscopeModelName)
                .temperature((float) dashscopeTemperature)
                .maxTokens(dashscopeMaxTokens)
                .build();
    }

    /**
     * Embedding 模型 —— RAG 检索的基础
     *
     * 将文本转换为高维向量（本例为384维）：
     *   - 语义相近的文本 → 向量距离相近
     *   - 用于文档切片的向量化存储和相似度检索
     *   - AllMiniLmL6V2：轻量级本地模型，无需调用外部 API
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        log.info("Initializing AllMiniLmL6V2 Embedding Model (384 dimensions)");
        return new AllMiniLmL6V2EmbeddingModel();
    }
}
