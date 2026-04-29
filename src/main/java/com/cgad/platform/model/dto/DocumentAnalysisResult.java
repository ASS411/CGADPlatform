package com.cgad.platform.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文档分析结果 —— 大模型输出的结构化数据
 *
 * 【知识点】@Builder 是 Lombok 的建造者模式注解：
 *   - 自动生成 Builder 内部类，支持链式创建对象
 *   - 使用方式：DocumentAnalysisResult.builder().summary("...").build()
 *   - 优点：参数多时比构造器更清晰，且支持可选参数
 *
 * 【知识点】@NoArgsConstructor / @AllArgsConstructor：
 *   - @NoArgsConstructor：生成无参构造器（Jackson 反序列化需要）
 *   - @AllArgsConstructor：生成全参构造器
 *   - Jackson 反序列化时需要无参构造器创建对象，再通过 setter 设置字段
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentAnalysisResult {

    /** 文档摘要：200字以内的核心内容概括 */
    private String summary;

    /** 识别出的文档类型（如：合同、研报、协议） */
    private String documentType;

    /** 关键条款列表：提取的金额、日期、违约责任等结构化条款 */
    private List<KeyClause> keyClauses;

    /** 关键要点列表：3-5个文档核心要点 */
    private List<String> keyPoints;

    /** 风险评估：识别文档中可能存在的风险点 */
    private String riskAssessment;
}
