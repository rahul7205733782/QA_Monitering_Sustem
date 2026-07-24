package com.rahul.dailytask.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rahul.dailytask.entity.Task;
import com.rahul.dailytask.repository.TaskRepository;

@RestController
public class ExcelController {

    @Autowired
    private TaskRepository taskRepository;

    @GetMapping("/api/export/excel")
    public ResponseEntity<byte[]> exportExcel() throws IOException {

        XSSFWorkbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            List<Task> tasks = taskRepository.findAll();
            Sheet sheet = workbook.createSheet("QA Tasks");

            // Header Style
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Data Style
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setWrapText(true);
            dataStyle.setAlignment(HorizontalAlignment.LEFT);

            // Header Row - Match your Task entity fields
            String[] columns = {
                "ID", "Date", "Client Name", "Project Name", "Hospital Name",
                "Employee Name", "Developer Name", "Environment", "Task Title",
                "Task Type", "Module Name", "Testing Type", "Scenario Tested",
                "Status", "UAT Status", "PROD Status", "Priority", "Severity",
                "Browser", "Device Name", "Tested Number", "Tested URL",
                "Created Date", "Start Time", "End Time", "Expected Result",
                "Actual Result", "Description", "Remarks", "Bug ID", "Screenshot"
            };

            Row header = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 4000);
            }

            // Data Rows
            int rowNum = 1;
            for (Task task : tasks) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(safe(task.getId()));
                row.createCell(1).setCellValue(safe(task.getTestDate()));
                row.createCell(2).setCellValue(safe(task.getClientName()));
                row.createCell(3).setCellValue(safe(task.getProjectName()));
                row.createCell(4).setCellValue(safe(task.getHospitalName()));
                row.createCell(5).setCellValue(safe(task.getEmployeeName()));
                row.createCell(6).setCellValue(safe(task.getDevName()));
                row.createCell(7).setCellValue(safe(task.getEnvironment()));
                row.createCell(8).setCellValue(safe(task.getTaskTitle()));
                row.createCell(9).setCellValue(safe(task.getTaskType()));
                row.createCell(10).setCellValue(safe(task.getModuleName()));
                row.createCell(11).setCellValue(safe(task.getTestingType()));
                row.createCell(12).setCellValue(safe(task.getScenario()));
                row.createCell(13).setCellValue(safe(task.getStatus()));
                row.createCell(14).setCellValue(safe(task.getUatStatus()));
                row.createCell(15).setCellValue(safe(task.getProdStatus()));
                row.createCell(16).setCellValue(safe(task.getPriority()));
                row.createCell(17).setCellValue(safe(task.getSeverity()));
                row.createCell(18).setCellValue(safe(task.getBrowser()));
                row.createCell(19).setCellValue(safe(task.getDeviceName()));
                row.createCell(20).setCellValue(safe(task.getTestedNumber()));
                row.createCell(21).setCellValue(safe(task.getTestedUrl()));
                row.createCell(22).setCellValue(safe(task.getCreatedDate()));
                row.createCell(23).setCellValue(safe(task.getStartTime()));
                row.createCell(24).setCellValue(safe(task.getEndTime()));
                row.createCell(25).setCellValue(safe(task.getExpectedResult()));
                row.createCell(26).setCellValue(safe(task.getActualResult()));
                row.createCell(27).setCellValue(safe(task.getDescription()));
                row.createCell(28).setCellValue(safe(task.getRemarks()));
                row.createCell(29).setCellValue(safe(task.getBugId()));
                row.createCell(30).setCellValue(safe(task.getScreenshot()));
                
                // Apply style to all cells
                for (int i = 0; i < columns.length; i++) {
                    if (row.getCell(i) != null) {
                        row.getCell(i).setCellStyle(dataStyle);
                    }
                }
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
                if (sheet.getColumnWidth(i) < 3000) {
                    sheet.setColumnWidth(i, 3000);
                }
                if (sheet.getColumnWidth(i) > 15000) {
                    sheet.setColumnWidth(i, 15000);
                }
            }

            // Freeze header
            sheet.createFreezePane(0, 1);
            sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, columns.length - 1));

            workbook.write(outputStream);

            String fileName = "QA_Tasks_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) +
                    ".xlsx";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(outputStream.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Failed to export Excel: " + e.getMessage());
        } finally {
            workbook.close();
            outputStream.close();
        }
    }

    private String safe(Object value) {
        return value == null || value.toString().equals("null") ? "" : value.toString();
    }
}