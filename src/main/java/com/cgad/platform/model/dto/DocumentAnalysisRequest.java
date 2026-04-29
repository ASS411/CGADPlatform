package com.cgad.platform.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 文档分析请求 DTO（Data Transfer Object）
 *
 * 【知识点】DTO 模式：
 *   - DTO 是用于层与层之间数据传输的对象
 *   - 与数据库实体（Entity）不同，DTO 只包含 API 需要的字段
 *   - 好处：解耦 API 接口与内部数据结构，各自可以独立变化
 *
 * 【知识点】@Data 是 Lombok 注解，自动生成：
 *   - getter / setter 方法
 *   - toString() 方法
 *   - equals() / hashCode() 方法
 *   - 等价于手写几十行样板代码
 *
 * 【知识点】@NotBlank 是 Bean Validation 注解：
 *   - 校验字符串不能为 null、空字符串或纯空格
 *   - 配合 Controller 中的 @Valid 使用，校验失败自动返回 400 错误
 *   - 类似注解：@NotNull（不能为null）、@NotEmpty（不能为空）、@Size（长度限制）
 */
@Data
public class DocumentAnalysisRequest {

    /** 待分析的文档文本内容 */
    @NotBlank(message = "文档内容不能为空")
    private String content;

    /** 文档类型提示（如：合同、研报），帮助模型更准确地分析 */
    private String documentType;

    /** 是否提取关键条款（金额、日期、违约责任等） */
    private boolean extractKeyClauses = true;

    /** 是否生成文档摘要 */
    private boolean generateSummary = true;
}
