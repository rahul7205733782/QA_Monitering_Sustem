package com.rahul.dailytask.controller;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rahul.dailytask.entity.Task;
import com.rahul.dailytask.repository.TaskRepository;

@RestController
public class CsvController {

    @Autowired
    private TaskRepository taskRepository;

    @GetMapping("/api/export/csv")
    public ResponseEntity<byte[]> exportCSV() {

        try {
            List<Task> tasks = taskRepository.findAll();
            StringBuilder csv = new StringBuilder();

            // CSV Header - Match your Task entity fields
            csv.append("ID,Date,Client Name,Project Name,Hospital Name,Employee Name,Developer Name,Environment,Task Title,Task Type,Module Name,Testing Type,Scenario Tested,Status,UAT Status,PROD Status,Priority,Severity,Browser,Device Name,Tested Number,Tested URL,Created Date,Start Time,End Time,Expected Result,Actual Result,Description,Remarks,Bug ID,Screenshot\n");

            // CSV Rows
            for (Task task : tasks) {
                csv.append(escapeCsv(String.valueOf(task.getId()))).append(",");
                csv.append(escapeCsv(task.getTestDate())).append(",");
                csv.append(escapeCsv(task.getClientName())).append(",");
                csv.append(escapeCsv(task.getProjectName())).append(",");
                csv.append(escapeCsv(task.getHospitalName())).append(",");
                csv.append(escapeCsv(task.getEmployeeName())).append(",");
                csv.append(escapeCsv(task.getDevName())).append(",");
                csv.append(escapeCsv(task.getEnvironment())).append(",");
                csv.append(escapeCsv(task.getTaskTitle())).append(",");
                csv.append(escapeCsv(task.getTaskType())).append(",");
                csv.append(escapeCsv(task.getModuleName())).append(",");
                csv.append(escapeCsv(task.getTestingType())).append(",");
                csv.append(escapeCsv(task.getScenario())).append(",");
                csv.append(escapeCsv(task.getStatus())).append(",");
                csv.append(escapeCsv(task.getUatStatus())).append(",");
                csv.append(escapeCsv(task.getProdStatus())).append(",");
                csv.append(escapeCsv(task.getPriority())).append(",");
                csv.append(escapeCsv(task.getSeverity())).append(",");
                csv.append(escapeCsv(task.getBrowser())).append(",");
                csv.append(escapeCsv(task.getDeviceName())).append(",");
                csv.append(escapeCsv(task.getTestedNumber())).append(",");
                csv.append(escapeCsv(task.getTestedUrl())).append(",");
                csv.append(escapeCsv(task.getCreatedDate())).append(",");
                csv.append(escapeCsv(task.getStartTime())).append(",");
                csv.append(escapeCsv(task.getEndTime())).append(",");
                csv.append(escapeCsv(task.getExpectedResult())).append(",");
                csv.append(escapeCsv(task.getActualResult())).append(",");
                csv.append(escapeCsv(task.getDescription())).append(",");
                csv.append(escapeCsv(task.getRemarks())).append(",");
                csv.append(escapeCsv(task.getBugId())).append(",");
                csv.append(escapeCsv(task.getScreenshot())).append("\n");
            }

            String fileName = "Tasks_Export_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) +
                    ".csv";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                    .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                    .body(csv.toString().getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Failed to export CSV".getBytes());
        }
    }

    private String escapeCsv(String value) {
        if (value == null || value.equals("null")) {
            return "";
        }
        value = value.replace("\"", "\"\"");
        return "\"" + value + "\"";
    }
}