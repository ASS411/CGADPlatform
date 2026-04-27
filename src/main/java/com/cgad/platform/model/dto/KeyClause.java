package com.cgad.platform.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeyClause {

    private String clauseType;

    private String clauseContent;

    private String clauseValue;

    private String location;
}
