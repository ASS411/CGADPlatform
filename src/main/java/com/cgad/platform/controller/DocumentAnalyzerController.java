package com.cgad.platform.controller;

import com.cgad.platform.model.dto.ApiResponse;
import com.cgad.platform.model.dto.DocumentAnalysisRequest;
import com.cgad.platform.model.dto.DocumentAnalysisResult;
import com.cgad.platform.service.DocumentAnalyzerService;
import com.cgad.platform.util.ExcelExporter;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/api/document")
public class DocumentAnalyzerController {

    private final DocumentAnalyzerService documentAnalyzerService;
    private final ExcelExporter excelExporter;

    public DocumentAnalyzerController(DocumentAnalyzerService documentAnalyzerService,
                                      ExcelExporter excelExporter) {
        this.documentAnalyzerService = documentAnalyzerService;
        this.excelExporter = excelExporter;
    }

    @PostMapping("/analyze")
    public ApiResponse<DocumentAnalysisResult> analyzeDocument(
            @Valid @RequestBody DocumentAnalysisRequest request) {
        log.info("Received document analysis request");
        DocumentAnalysisResult result = documentAnalyzerService.analyzeDocument(request);
        return ApiResponse.success(result);
    }

    @PostMapping("/analyze/file")
    public ApiResponse<DocumentAnalysisResult> analyzeFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "documentType", required = false) String documentType) throws IOException {
        log.info("Received file analysis request, filename: {}", file.getOriginalFilename());

        String content = new String(file.getBytes(), StandardCharsets.UTF_8);

        DocumentAnalysisRequest request = new DocumentAnalysisRequest();
        request.setContent(content);
        request.setDocumentType(documentType);

        DocumentAnalysisResult result = documentAnalyzerService.analyzeDocument(request);
        return ApiResponse.success(result);
    }

    @PostMapping("/analyze/export-excel")
    public ResponseEntity<Resource> analyzeAndExportExcel(
            @Valid @RequestBody DocumentAnalysisRequest request) throws IOException {
        log.info("Received document analysis with Excel export request");

        DocumentAnalysisResult result = documentAnalyzerService.analyzeDocument(request);
        Resource excelResource = excelExporter.exportAnalysisResult(result);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"analysis-result.xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelResource);
    }
}
