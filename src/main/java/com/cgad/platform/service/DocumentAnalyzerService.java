package com.cgad.platform.service;

import com.cgad.platform.model.dto.DocumentAnalysisRequest;
import com.cgad.platform.model.dto.DocumentAnalysisResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 智能文档分析服务 —— 本项目的核心功能之一
 *
 * 利用大模型的生成和理解能力，对非结构化文档进行结构化处理：
 *   - 输入：一段非结构化的文本（研报、合同等）
 *   - 输出：结构化的 JSON 数据（摘要、关键条款、要点、风险评估）
 *
 * 结构化输出（Structured Output）：
 *   - 大模型默认输出自由文本，无法直接被程序解析
 *   - 通过 Prompt Engineering 在提示词中指定 JSON Schema，强制模型输出结构化数据
 *   - 再用 Jackson 将 JSON 反序列化为 Java 对象，便于后端直接入库
 */
@Slf4j
@Service
public class DocumentAnalyzerService {

    /**
     * 聊天语言模型 —— LangChain4j 的核心接口
     */
    private final ChatLanguageModel chatModel;

    /**
     * 用于将模型输出的 JSON 字符串转换为 Java 对象
     */
    private final ObjectMapper objectMapper;

    /**
     * 构造器注入
     */
    public DocumentAnalyzerService(ChatLanguageModel chatModel, ObjectMapper objectMapper) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
    }

    /**
     * 分析文档 —— 主入口方法
     *
     * 处理流程：
     *   1. 根据请求参数构建提示词（Prompt）
     *   2. 调用大模型生成响应
     *   3. 解析模型输出为结构化的 Java 对象
     *
     * chatModel.generate() 是 LangChain4j 的核心调用方法：
     *   - 输入：一段文本提示词
     *   - 输出：模型生成的文本
     *   - 本质是调用大模型的 HTTP API，LangChain4j 封装了底层细节
     */
    public DocumentAnalysisResult analyzeDocument(DocumentAnalysisRequest request) {
        // 构建提示词
        String prompt = buildAnalysisPrompt(request);
        log.info("Sending document analysis request, content length: {}", request.getContent().length());

        // 调用大模型
        String rawResponse = chatModel.chat(prompt);
        log.debug("Raw model response: {}", rawResponse);

        // 第三步：解析结构化输出
        return parseStructuredOutput(rawResponse);
    }

    /**
     * 构建文档分析的提示词（Prompt Engineering 的核心）
        提示词包含：角色设定 + 任务描述 + 输出格式 + 输入数据
     *   1. 角色设定："你是一个专业的文档分析助手"
     *   2. 分析要求：根据用户选项动态生成（摘要/条款/要点/风险）
     *   3. 输出格式：指定 JSON Schema，确保模型输出可解析的结构化数据
     *   4. 文档内容：待分析的实际文本
     */
    private String buildAnalysisPrompt(DocumentAnalysisRequest request) {
        StringBuilder sb = new StringBuilder();

        // 角色设定：告诉模型它应该扮演什么角色
        sb.append("你是一个专业的文档分析助手。请对以下文档进行深度分析，并严格按照JSON格式输出结果。\n\n");

        // 任务描述：明确告诉模型需要做什么
        sb.append("## 分析要求\n");
        if (request.isGenerateSummary()) {
            sb.append("1. 生成文档摘要（summary字段）：用200字以内概括文档核心内容\n");
        }
        if (request.isExtractKeyClauses()) {
            sb.append("2. 提取关键条款（keyClauses数组）：提取文档中的关键条款，每条包含：\n");
            sb.append("   - clauseType: 条款类型（如：金额/日期/违约责任/保密条款/知识产权/付款条件等）\n");
            sb.append("   - clauseContent: 条款原文内容\n");
            sb.append("   - clauseValue: 条款的具体数值或关键信息\n");
            sb.append("   - location: 条款在文档中的位置描述\n");
        }
        sb.append("3. 提取关键要点（keyPoints数组）：列出3-5个文档的核心要点\n");
        sb.append("4. 风险评估（riskAssessment字段）：识别文档中可能存在的风险点\n");

        // 上下文提示：帮助模型更好地理解文档
        if (request.getDocumentType() != null && !request.getDocumentType().isBlank()) {
            sb.append("\n文档类型提示：").append(request.getDocumentType()).append("\n");
        }

        // 输出格式：指定 JSON Schema，这是实现结构化输出的关键
        // 【关键】在提示词中给出完整的 JSON 示例，模型会模仿这个格式输出
        sb.append("\n## 输出格式\n");
        sb.append("请严格按照以下JSON格式输出，不要输出任何其他内容：\n");
        sb.append("```json\n");
        sb.append("{\n");
        sb.append("  \"summary\": \"文档摘要\",\n");
        sb.append("  \"documentType\": \"识别出的文档类型\",\n");
        sb.append("  \"keyClauses\": [\n");
        sb.append("    {\n");
        sb.append("      \"clauseType\": \"条款类型\",\n");
        sb.append("      \"clauseContent\": \"条款内容\",\n");
        sb.append("      \"clauseValue\": \"具体数值\",\n");
        sb.append("      \"location\": \"位置描述\"\n");
        sb.append("    }\n");
        sb.append("  ],\n");
        sb.append("  \"keyPoints\": [\"要点1\", \"要点2\", \"要点3\"],\n");
        sb.append("  \"riskAssessment\": \"风险评估\"\n");
        sb.append("}\n");
        sb.append("```\n\n");

        // 输入数据：待分析的实际文档内容
        sb.append("## 文档内容\n");
        sb.append(request.getContent());

        return sb.toString();
    }

    /**
     * 解析模型输出为结构化的 Java 对象
     *
     * 大模型输出不稳定性的应对策略：
     *   - 模型可能输出额外的说明文字（如"以下是分析结果：..."）
     *   - 模型可能输出 Markdown 代码块包裹的 JSON
     *   - 因此需要先提取纯 JSON，再进行反序列化
     *   - 如果解析失败，使用降级策略（将原始输出作为摘要返回）
     */
    private DocumentAnalysisResult parseStructuredOutput(String rawResponse) {
        try {
            // 先提取纯 JSON 字符串（去除模型可能添加的额外文字）
            String jsonStr = extractJson(rawResponse);
            // 用 Jackson 反序列化为 Java 对象
            return objectMapper.readValue(jsonStr, DocumentAnalysisResult.class);
        } catch (Exception e) {
            // 解析失败时的降级策略：将原始输出作为摘要返回，避免程序崩溃
            log.error("Failed to parse structured output, attempting fallback parsing", e);
            return DocumentAnalysisResult.builder()
                    .summary(rawResponse)
                    .build();
        }
    }

    /**
     * 从模型输出中提取 JSON 字符串
     *
     * 模型输出可能包含：
     *   - 纯 JSON：{"summary": "..."}
     *   - 带说明的 JSON："分析结果如下：\n{"summary": "..."}"
     *   - Markdown 代码块：```json\n{"summary": "..."}\n```
     *
     * 本方法通过查找第一个 { 和最后一个 } 来提取 JSON
     */
    private String extractJson(String text) {
        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }
}
