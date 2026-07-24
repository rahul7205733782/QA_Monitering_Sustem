package com.rahul.dailytask.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rahul.dailytask.entity.JsonSheet;
import com.rahul.dailytask.repository.JsonSheetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/json-sheets")
@CrossOrigin(origins = "*")
public class JsonSheetController {

    @Autowired    private JsonSheetRepository jsonSheetRepository;

    @GetMapping
    public ResponseEntity<List<JsonSheet>> getAllJsonSheets() {
        List<JsonSheet> sheets = jsonSheetRepository.findByOrderByCreatedDateDesc();
        return ResponseEntity.ok(sheets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JsonSheet> getJsonSheetById(@PathVariable Long id) {
        return jsonSheetRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<JsonSheet>> searchByModule(@RequestParam String module) {
        List<JsonSheet> sheets = jsonSheetRepository.findByModuleNameContainingIgnoreCase(module);
        return ResponseEntity.ok(sheets);
    }

    @PostMapping
    public ResponseEntity<?> createJsonSheet(@RequestBody JsonSheet jsonSheet) {
        try {
            jsonSheet.setCreatedDate(LocalDateTime.now());
            jsonSheet.setFileSize((long) jsonSheet.getJsonContent().length());
            JsonSheet saved = jsonSheetRepository.save(jsonSheet);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving JSON sheet: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateJsonSheet(@PathVariable Long id, @RequestBody JsonSheet jsonSheet) {
        return jsonSheetRepository.findById(id)
                .map(existing -> {
                    existing.setModuleName(jsonSheet.getModuleName());
                    existing.setJsonFileName(jsonSheet.getJsonFileName());
                    existing.setJsonContent(jsonSheet.getJsonContent());
                    existing.setUpdatedDate(LocalDateTime.now());
                    existing.setFileSize((long) jsonSheet.getJsonContent().length());
                    JsonSheet updated = jsonSheetRepository.save(existing);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteJsonSheet(@PathVariable Long id) {
        if (!jsonSheetRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        jsonSheetRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // =====================================
    // DOWNLOAD JSON FILE
    // =====================================
    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadJsonFile(@PathVariable Long id) {
        return jsonSheetRepository.findById(id)
                .map(sheet -> {
                    try {
                        String content = sheet.getJsonContent();
                        try {
                            ObjectMapper mapper = new ObjectMapper();
                            Object json = mapper.readValue(content, Object.class);
                            content = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
                        } catch (Exception e) {
                            // Keep as is
                        }
                        
                        byte[] data = content.getBytes(StandardCharsets.UTF_8);
                        return ResponseEntity.ok()
                                .header("Content-Disposition", "attachment; filename=\"" + sheet.getJsonFileName() + "\"")
                                .header("Content-Type", "application/json")
                                .body(data);
                    } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Error downloading file: " + e.getMessage());
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // =====================================
    // OPEN IN VS CODE
    // =====================================
    @GetMapping("/open-vscode/{id}")
    public ResponseEntity<?> openInVSCode(@PathVariable Long id) {
        return jsonSheetRepository.findById(id)
                .map(sheet -> {
                    try {
                        // Create temp file
                        String tempDir = System.getProperty("java.io.tmpdir");
                        String fileName = sheet.getJsonFileName();
                        if (!fileName.endsWith(".json")) {
                            fileName = fileName + ".json";
                        }
                        
                        Path tempFile = Paths.get(tempDir, fileName);
                        
                        // Format JSON content
                        String content = sheet.getJsonContent();
                        try {
                            ObjectMapper mapper = new ObjectMapper();
                            Object json = mapper.readValue(content, Object.class);
                            content = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
                        } catch (Exception e) {
                            // Keep as is
                        }
                        
                        Files.write(tempFile, content.getBytes(StandardCharsets.UTF_8));
                        
                        // VS Code path (update if different)
                        String vscodePath = "\"C:\\Users\\Rahul Kumar\\AppData\\Local\\Programs\\Microsoft VS Code\\Code.exe\"";
                        String command = vscodePath + " \"" + tempFile.toString() + "\"";
                        
                        // Execute command
                        Process process = Runtime.getRuntime().exec(command);
                        
                        return ResponseEntity.ok(Map.of(
                            "message", "File opened in VS Code",
                            "file", tempFile.toString(),
                            "fileName", sheet.getJsonFileName()
                        ));
                        
                    } catch (IOException e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Error opening in VS Code: " + e.getMessage());
                    } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Error: " + e.getMessage());
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }
}