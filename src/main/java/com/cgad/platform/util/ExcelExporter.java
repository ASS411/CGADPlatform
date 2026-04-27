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

@Component
public class ExcelExporter {

    public Resource exportAnalysisResult(DocumentAnalysisResult result) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            createSummarySheet(workbook, result);
            createKeyClausesSheet(workbook, result);
            createKeyPointsSheet(workbook, result);

            workbook.write(outputStream);
            return new ByteArrayResource(outputStream.toByteArray());
        }
    }

    private void createSummarySheet(Workbook workbook, DocumentAnalysisResult result) {
        Sheet sheet = workbook.createSheet("文档摘要");

        CellStyle headerStyle = createHeaderStyle(workbook);

        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "项目", headerStyle);
        createCell(headerRow, 1, "内容", headerStyle);

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

        sheet.setColumnWidth(0, 5000);
        sheet.setColumnWidth(1, 15000);
    }

    private void createKeyClausesSheet(Workbook workbook, DocumentAnalysisResult result) {
        Sheet sheet = workbook.createSheet("关键条款");

        CellStyle headerStyle = createHeaderStyle(workbook);

        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "条款类型", headerStyle);
        createCell(headerRow, 1, "条款内容", headerStyle);
        createCell(headerRow, 2, "具体数值", headerStyle);
        createCell(headerRow, 3, "位置", headerStyle);

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
