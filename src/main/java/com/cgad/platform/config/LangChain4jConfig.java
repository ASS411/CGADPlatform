package com.cgad.platform.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    @Value("${langchain4j.open-ai.temperature:0.1f}")
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
}
