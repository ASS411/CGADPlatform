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
public class TranslationResult {

    private String translatedText;

    private String detectedSourceLanguage;

    private List<String> glossaryNotes;

    private double confidence;
}
