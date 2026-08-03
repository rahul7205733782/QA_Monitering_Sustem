package com.rahul.dailytask.controller;

import com.rahul.dailytask.entity.ResponseSheetHistory;
import com.rahul.dailytask.service.ResponseSheetService;
import utils.JsonToExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/response-sheet")
@CrossOrigin(origins = "*")
public class ResponseSheetController {

    @Autowired
    private ResponseSheetService responseSheetService;

    // =====================================
    // DETECT LANGUAGES
    // =====================================
    @PostMapping("/detect-languages")
    public ResponseEntity<?> detectLanguages(@RequestBody Map<String, String> request) {
        try {
            System.out.println("🔍 /detect-languages called");
            String jsonData = request.get("jsonData");
            System.out.println("📊 jsonData length: " + (jsonData != null ? jsonData.length() : "null"));

            if (jsonData == null || jsonData.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "jsonData is required"));
            }

            List<String> languages = responseSheetService.detectLanguages(jsonData);
            System.out.println("🌐 Languages detected: " + languages);

            List<Map<String, String>> languageInfo = languages.stream()
                    .map(lang -> {
                        Map<String, String> info = new HashMap<>();
                        info.put("field", lang);
                        info.put("displayName", JsonToExcelUtil.getLanguageDisplayName(lang));
                        return info;
                    })
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("languages", languageInfo);
            response.put("totalLanguages", languages.size());

            System.out.println("📤 Response: " + response);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // =====================================
    // GENERATE EXCEL - FIXED TO ACCEPT HEADERS
    // =====================================
    @PostMapping("/generate")
    public ResponseEntity<?> generateExcel(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("📊 /generate called");

            String jsonData = (String) request.get("jsonData");
            String fileName = (String) request.getOrDefault("fileName", "Response_Sheet");
            List<String> selectedLanguages = (List<String>) request.get("selectedLanguages");
            List<String> languageHeaders = (List<String>) request.get("languageHeaders"); // <--- GET THIS
            String uploadedBy = (String) request.getOrDefault("uploadedBy", "system");

            // Safety fallback: If frontend didn't send headers, generate them from raw fields
            if (languageHeaders == null || languageHeaders.isEmpty()) {
                languageHeaders = selectedLanguages.stream()
                        .map(JsonToExcelUtil::getLanguageDisplayName)
                        .toList();
            }

            if (jsonData == null || jsonData.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "jsonData is required"));
            }

            if (selectedLanguages == null || selectedLanguages.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Please select at least one language"));
            }

            // Pass BOTH lists to the service
            ResponseSheetHistory history = responseSheetService.generateExcel(
                    jsonData, fileName, selectedLanguages, languageHeaders, uploadedBy
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "✅ Excel generated successfully!");
            response.put("historyId", history.getId());
            response.put("fileName", history.getFileName());
            response.put("selectedLanguages", selectedLanguages);
            response.put("totalRecords", history.getTotalRecords());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // =====================================
    // DOWNLOAD EXCEL
    // =====================================
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadExcel(@PathVariable Long id) {
        try {
            System.out.println("📥 Download request for ID: " + id);

            String filePath = responseSheetService.getFilePath(id);
            if (filePath == null) {
                return ResponseEntity.notFound().build();
            }

            File file = new File(filePath);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            responseSheetService.incrementDownloadCount(id);
            Resource resource = new FileSystemResource(file);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + file.getName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // =====================================
    // GET HISTORY
    // =====================================
    @GetMapping("/history")
    public ResponseEntity<?> getHistory(@RequestParam(required = false) String uploadedBy) {
        try {
            System.out.println("📋 Fetching history" + (uploadedBy != null ? " for user: " + uploadedBy : ""));

            List<ResponseSheetHistory> history;
            if (uploadedBy != null && !uploadedBy.isEmpty()) {
                history = responseSheetService.getHistoryByUser(uploadedBy);
            } else {
                history = responseSheetService.getHistory();
            }

            System.out.println("📋 Found " + history.size() + " history records");
            return ResponseEntity.ok(history);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // =====================================
    // DELETE HISTORY RECORD
    // =====================================
    @DeleteMapping("/history/{id}")
    public ResponseEntity<?> deleteHistory(@PathVariable Long id) {
        try {
            System.out.println("🗑️ Deleting history record ID: " + id);

            String filePath = responseSheetService.getFilePath(id);
            boolean deleted = responseSheetService.deleteHistory(id);

            if (!deleted) {
                System.out.println("❌ History record not found: " + id);
                return ResponseEntity.notFound().build();
            }

            if (filePath != null) {
                File file = new File(filePath);
                if (file.exists()) {
                    boolean fileDeleted = file.delete();
                    System.out.println("📄 Excel file deleted: " + fileDeleted + " - " + filePath);
                }
            }

            System.out.println("✅ History record deleted successfully: " + id);
            return ResponseEntity.ok(Map.of("message", "✅ History record deleted successfully!"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // =====================================
    // CLEAR ALL HISTORY
    // =====================================
    @DeleteMapping("/history/clear-all")
    public ResponseEntity<?> clearAllHistory() {
        try {
            System.out.println("🗑️ Clearing ALL history records");

            List<ResponseSheetHistory> histories = responseSheetService.getHistory();

            int filesDeleted = 0;
            for (ResponseSheetHistory history : histories) {
                String filePath = history.getFilePath();
                if (filePath != null) {
                    File file = new File(filePath);
                    if (file.exists()) {
                        boolean deleted = file.delete();
                        if (deleted) filesDeleted++;
                        System.out.println("📄 Excel file deleted: " + deleted + " - " + filePath);
                    }
                }
            }

            responseSheetService.clearAllHistory();

            System.out.println("✅ All history cleared! Files deleted: " + filesDeleted);
            return ResponseEntity.ok(Map.of(
                    "message", "✅ All history records cleared!",
                    "filesDeleted", filesDeleted
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}