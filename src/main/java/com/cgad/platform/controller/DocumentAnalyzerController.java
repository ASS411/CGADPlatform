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

/**
 * 智能文档分析控制器
 */
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

    /**
     * 文档分析接口
     * 请求示例：
     *   POST /api/document/analyze
     *   Content-Type: application/json
     *   { "content": "合同内容...", "documentType": "合同" }
     */
    @PostMapping("/analyze")
    public ApiResponse<DocumentAnalysisResult> analyzeDocument(
            @Valid @RequestBody DocumentAnalysisRequest request) {
        log.info("Received document analysis request");
        DocumentAnalysisResult result = documentAnalyzerService.analyzeDocument(request);
        return ApiResponse.success(result);
    }

    /**
     * 文件上传分析接口
     */
    @PostMapping("/analyze/file")
    public ApiResponse<DocumentAnalysisResult> analyzeFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "documentType", required = false) String documentType) throws IOException {
        log.info("Received file analysis request, filename: {}", file.getOriginalFilename());

        // 将上传文件的内容转为 UTF-8 字符串
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);

        DocumentAnalysisRequest request = new DocumentAnalysisRequest();
        request.setContent(content);
        request.setDocumentType(documentType);

        DocumentAnalysisResult result = documentAnalyzerService.analyzeDocument(request);
        return ApiResponse.success(result);
    }

    /**
     * 分析并导出 Excel 接口
     *
     * ResponseEntity：Spring 的 HTTP 响应封装，可精细控制：
     *   - 响应状态码
     *   - 响应头（如 Content-Disposition 控制文件下载）
     *   - 响应体（文件内容）
     *
     * 文件下载的关键响应头：
     *   - Content-Disposition: attachment; filename="xxx.xlsx" —— 告诉浏览器下载而非显示
     *   - Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
     *     —— Excel 文件的 MIME 类型
     */
    @PostMapping("/analyze/export-excel")
    public ResponseEntity<Resource> analyzeAndExportExcel(
            @Valid @RequestBody DocumentAnalysisRequest request) throws IOException {
        log.info("Received document analysis with Excel export request");

        // 先分析文档，获取结构化结果
        DocumentAnalysisResult result = documentAnalyzerService.analyzeDocument(request);
        // 将结果导出为 Excel 文件
        Resource excelResource = excelExporter.exportAnalysisResult(result);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"analysis-result.xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelResource);
    }
}
