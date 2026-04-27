package com.cgad.platform.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentAnalysisResult {

    private String summary;

    private String documentType;

    private List<KeyClause> keyClauses;

    private List<String> keyPoints;

    private String riskAssessment;
}
