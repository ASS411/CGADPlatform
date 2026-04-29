package com.cgad.platform.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 关键词提取结果 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeywordExtractionResult {

    /** 提取的关键词列表 */
    private List<KeywordItem> keywords;

    /** 文本主题概括（一句话） */
    private String topicSummary;

    /** 文本情感倾向：正面/中性/负面 */
    private String sentiment;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeywordItem {
        /** 关键词 */
        private String keyword;
        /** 权重/重要度 0-1 */
        private double relevance;
        /** 关键词分类（如：实体、术语、主题） */
        private String category;
    }
}
