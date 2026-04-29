package com.cgad.platform.util;

import com.cgad.platform.model.dto.DocumentAnalysisResult;
import com.cgad.platform.model.dto.KeyClause;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Excel 导出工具 —— 将文档分析结果导出为 .xlsx 文件
 *
 * 【知识点】Apache POI 是 Java 操作 Office 文件的标准库：
 *   - XSSFWorkbook：对应 .xlsx 格式（Office 2007+）
 *   - HSSFWorkbook：对应 .xls 格式（旧版 Office），本项目不使用
 *
 * 【知识点】POI 核心对象模型：
 *   - Workbook（工作簿）→ 包含多个 Sheet
 *   - Sheet（工作表）→ 包含多行 Row
 *   - Row（行）→ 包含多个 Cell
 *   - Cell（单元格）→ 存储具体数据
 *
 * 【知识点】@Component 是 Spring 的通用组件注解：
 *   - 与 @Service、@Repository 功能相同，只是语义不同
 *   - @Service 表示业务逻辑，@Repository 表示数据访问，@Component 表示工具类
 *
 * 【知识点】try-with-resources 语法：
 *   - 自动关闭实现了 AutoCloseable 接口的资源
 *   - 等价于 finally { workbook.close(); }，但更简洁安全
 */
@Component
public class ExcelExporter {

    /**
     * 将分析结果导出为 Excel 文件资源
     *
     * 导出的 Excel 包含三个工作表：
     *   1. 文档摘要 —— 摘要、文档类型、风险评估
     *   2. 关键条款 —— 条款类型、内容、数值、位置
     *   3. 关键要点 —— 序号、要点内容
     *
     * 【知识点】ByteArrayResource：
     *   - Spring 的 Resource 接口实现，将 byte[] 包装为资源
     *   - Controller 可以直接将 Resource 作为 ResponseEntity 的 body 返回
     *   - Spring 会自动将 byte[] 写入 HTTP 响应流
     */
    public Resource exportAnalysisResult(DocumentAnalysisResult result) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // 创建三个工作表
            createSummarySheet(workbook, result);
            createKeyClausesSheet(workbook, result);
            createKeyPointsSheet(workbook, result);

            // 将 Workbook 写入内存流，再转为 byte[]
            workbook.write(outputStream);
            return new ByteArrayResource(outputStream.toByteArray());
        }
    }

    /** 创建"文档摘要"工作表 */
    private void createSummarySheet(Workbook workbook, DocumentAnalysisResult result) {
        Sheet sheet = workbook.createSheet("文档摘要");

        CellStyle headerStyle = createHeaderStyle(workbook);

        // 表头行
        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "项目", headerStyle);
        createCell(headerRow, 1, "内容", headerStyle);

        // 数据行
        int rowNum = 1;
        if (result.getSummary() != null) {
            Row row = sheet.createRow(rowNum++);
            createCell(row, 0, "摘要");
            createCell(row, 1, result.getSummary());
        }
        if (result.getDocumentType() != null) {
            Row row = sheet.createRow(rowNum++);
            createCell(row, 0, "文档类型");
            createCell(row, 1, result.getDocumentType());
        }
        if (result.getRiskAssessment() != null) {
            Row row = sheet.createRow(rowNum++);
            createCell(row, 0, "风险评估");
            createCell(row, 1, result.getRiskAssessment());
        }

        // 设置列宽（单位：1/256 字符宽度）
        sheet.setColumnWidth(0, 5000);
        sheet.setColumnWidth(1, 15000);
    }

    /** 创建"关键条款"工作表 */
    private void createKeyClausesSheet(Workbook workbook, DocumentAnalysisResult result) {
        Sheet sheet = workbook.createSheet("关键条款");

        CellStyle headerStyle = createHeaderStyle(workbook);

        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "条款类型", headerStyle);
        createCell(headerRow, 1, "条款内容", headerStyle);
        createCell(headerRow, 2, "具体数值", headerStyle);
        createCell(headerRow, 3, "位置", headerStyle);

        // 遍历关键条款列表，逐行写入
        if (result.getKeyClauses() != null) {
            int rowNum = 1;
            for (KeyClause clause : result.getKeyClauses()) {
                Row row = sheet.createRow(rowNum++);
                createCell(row, 0, clause.getClauseType());
                createCell(row, 1, clause.getClauseContent());
                createCell(row, 2, clause.getClauseValue());
                createCell(row, 3, clause.getLocation());
            }
        }

        for (int i = 0; i < 4; i++) {
            sheet.setColumnWidth(i, 8000);
        }
    }

    /** 创建"关键要点"工作表 */
    private void createKeyPointsSheet(Workbook workbook, DocumentAnalysisResult result) {
        Sheet sheet = workbook.createSheet("关键要点");

        CellStyle headerStyle = createHeaderStyle(workbook);

        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "序号", headerStyle);
        createCell(headerRow, 1, "要点", headerStyle);

        if (result.getKeyPoints() != null) {
            int rowNum = 1;
            for (int i = 0; i < result.getKeyPoints().size(); i++) {
                Row row = sheet.createRow(rowNum++);
                createCell(row, 0, String.valueOf(i + 1));
                createCell(row, 1, result.getKeyPoints().get(i));
            }
        }

        sheet.setColumnWidth(0, 3000);
        sheet.setColumnWidth(1, 15000);
    }

    /** 创建表头样式：加粗 + 灰色背景 */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private void createCell(Row row, int column, String value) {
        createCell(row, column, value, null);
    }

    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value != null ? value : "");
        if (style != null) {
            cell.setCellStyle(style);
        }
    }
}
