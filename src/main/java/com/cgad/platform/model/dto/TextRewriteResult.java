package com.cgad.platform.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文本改写结果 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextRewriteResult {

    /** 改写后的文本 */
    private String rewrittenText;

    /** 改写说明：做了哪些修改 */
    private String changeSummary;

    /** 修改要点列表 */
    private List<String> changePoints;

    /** 质量评分 0-100 */
    private int qualityScore;
}
