package com.rahul.dailytask.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rahul.dailytask.entity.JourneyMap;
import com.rahul.dailytask.repository.JourneyMapRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/journey-maps")
@CrossOrigin(origins = "*")
public class JourneyMapController {

    @Autowired
    private JourneyMapRepository journeyMapRepository;

    // =====================================
    // GET ALL JOURNEYS
    // =====================================
    @GetMapping
    public ResponseEntity<List<JourneyMap>> getAllJourneys() {
        List<JourneyMap> journeys = journeyMapRepository.findByOrderByCreatedDateDesc();
        System.out.println("📊 Found " + journeys.size() + " journeys");
        
        // Log PDF data presence
        for (JourneyMap j : journeys) {
            if (j.getPdfData() != null && !j.getPdfData().isEmpty()) {
                System.out.println("  ✅ " + j.getName() + " has PDF (" + j.getPdfData().length() + " chars)");
            }
        }
        
        return ResponseEntity.ok(journeys);
    }

    // =====================================
    // GET JOURNEY BY ID
    // =====================================
    @GetMapping("/{id}")
    public ResponseEntity<JourneyMap> getJourneyById(@PathVariable Long id) {
        return journeyMapRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =====================================
    // SEARCH JOURNEYS
    // =====================================
    @GetMapping("/search")
    public ResponseEntity<List<JourneyMap>> searchJourneys(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String status) {
        
        if (name != null && !name.isEmpty()) {
            return ResponseEntity.ok(journeyMapRepository.findByNameContainingIgnoreCase(name));
        } else if (module != null && !module.isEmpty()) {
            return ResponseEntity.ok(journeyMapRepository.findByModuleContainingIgnoreCase(module));
        } else if (status != null && !status.isEmpty()) {
            return ResponseEntity.ok(journeyMapRepository.findByStatus(status));
        }
        
        return ResponseEntity.ok(journeyMapRepository.findByOrderByCreatedDateDesc());
    }

    // =====================================
    // CREATE JOURNEY - UPDATED WITH PDF SUPPORT
    // =====================================
    @PostMapping
    public ResponseEntity<?> createJourney(@RequestBody JourneyMap journeyMap) {
        try {
            System.out.println("📥 ========================================");
            System.out.println("📥 Received Journey Map:");
            System.out.println("  Name: " + journeyMap.getName());
            System.out.println("  Module: " + journeyMap.getModule());
            System.out.println("  Sheet Link: " + journeyMap.getSheetLink());
            System.out.println("  PDF Link: " + journeyMap.getPdfLink());
            System.out.println("  PDF File Name: " + journeyMap.getPdfFileName());
            
            // Check PDF data
            if (journeyMap.getPdfData() != null && !journeyMap.getPdfData().isEmpty()) {
                System.out.println("  PDF Data: ✅ Present (" + journeyMap.getPdfData().length() + " characters)");
                System.out.println("  PDF Data Preview: " + journeyMap.getPdfData().substring(0, Math.min(100, journeyMap.getPdfData().length())) + "...");
            } else {
                System.out.println("  PDF Data: ❌ NULL or Empty");
            }

            // Validate stages JSON
            String stagesJson = journeyMap.getStagesJson();
            if (stagesJson != null && !stagesJson.isEmpty()) {
                ObjectMapper mapper = new ObjectMapper();
                Object json = mapper.readValue(stagesJson, Object.class);
                journeyMap.setStagesJson(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json));
            } else {
                journeyMap.setStagesJson("[]");
            }

            // Calculate progress
            int progress = calculateProgress(journeyMap.getStagesJson());
            journeyMap.setProgress(progress);

            // Set timestamps
            journeyMap.setCreatedDate(LocalDateTime.now());
            journeyMap.setUpdatedDate(LocalDateTime.now());

            // Set default status if not provided
            if (journeyMap.getStatus() == null || journeyMap.getStatus().isEmpty()) {
                journeyMap.setStatus("not-started");
            }

            // Set default created_by if not provided
            if (journeyMap.getCreatedBy() == null || journeyMap.getCreatedBy().isEmpty()) {
                journeyMap.setCreatedBy("system");
            }

            // Save to database
            JourneyMap saved = journeyMapRepository.save(journeyMap);
            System.out.println("✅ Journey saved with ID: " + saved.getId());
            
            // Verify PDF was saved
            if (saved.getPdfData() != null && !saved.getPdfData().isEmpty()) {
                System.out.println("✅ PDF Data saved successfully (" + saved.getPdfData().length() + " chars)");
            } else {
                System.out.println("⚠️ PDF Data was NOT saved!");
            }
            
            System.out.println("📥 ========================================");
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (Exception e) {
            System.err.println("❌ Error creating journey: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating journey: " + e.getMessage());
        }
    }

    // =====================================
    // UPDATE JOURNEY - UPDATED WITH PDF SUPPORT
    // =====================================
    @PutMapping("/{id}")
    public ResponseEntity<?> updateJourney(@PathVariable Long id, @RequestBody JourneyMap journeyMap) {
        return journeyMapRepository.findById(id)
                .map(existing -> {
                    try {
                        System.out.println("📝 Updating journey: " + id);
                        
                        // Update basic fields
                        existing.setName(journeyMap.getName());
                        existing.setModule(journeyMap.getModule());
                        existing.setDescription(journeyMap.getDescription());
                        existing.setStatus(journeyMap.getStatus());
                        existing.setEstimatedCompletion(journeyMap.getEstimatedCompletion());
                        existing.setUpdatedDate(LocalDateTime.now());

                        // Update Sheet Link
                        if (journeyMap.getSheetLink() != null) {
                            existing.setSheetLink(journeyMap.getSheetLink());
                        }

                        // Update PDF Link
                        if (journeyMap.getPdfLink() != null) {
                            existing.setPdfLink(journeyMap.getPdfLink());
                        }

                        // Update PDF Data (if provided)
                        if (journeyMap.getPdfData() != null && !journeyMap.getPdfData().isEmpty()) {
                            existing.setPdfData(journeyMap.getPdfData());
                            existing.setPdfFileName(journeyMap.getPdfFileName());
                            System.out.println("✅ PDF Data updated (" + journeyMap.getPdfData().length() + " chars)");
                        }

                        // Update stages JSON if provided
                        if (journeyMap.getStagesJson() != null && !journeyMap.getStagesJson().isEmpty()) {
                            ObjectMapper mapper = new ObjectMapper();
                            Object json = mapper.readValue(journeyMap.getStagesJson(), Object.class);
                            existing.setStagesJson(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json));
                        }

                        // Recalculate progress
                        int progress = calculateProgress(existing.getStagesJson());
                        existing.setProgress(progress);

                        // Auto-update status based on progress if not explicitly set
                        if (journeyMap.getStatus() == null || journeyMap.getStatus().isEmpty()) {
                            if (progress == 100) {
                                existing.setStatus("completed");
                            } else if (progress > 0) {
                                existing.setStatus("in-progress");
                            } else {
                                existing.setStatus("not-started");
                            }
                        }

                        JourneyMap updated = journeyMapRepository.save(existing);
                        System.out.println("✅ Journey updated: " + id);
                        return ResponseEntity.ok(updated);

                    } catch (Exception e) {
                        System.err.println("❌ Error updating journey: " + e.getMessage());
                        e.printStackTrace();
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Error updating journey: " + e.getMessage());
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // =====================================
    // DELETE JOURNEY
    // =====================================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteJourney(@PathVariable Long id) {
        if (!journeyMapRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        journeyMapRepository.deleteById(id);
        System.out.println("🗑️ Journey deleted: " + id);
        return ResponseEntity.ok().build();
    }

    // =====================================
    // GET JOURNEY STATISTICS
    // =====================================
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getJourneyStats() {
        try {
            List<JourneyMap> all = journeyMapRepository.findAll();
            
            long total = all.size();
            long completed = all.stream().filter(j -> "completed".equals(j.getStatus())).count();
            long inProgress = all.stream().filter(j -> "in-progress".equals(j.getStatus())).count();
            long blocked = all.stream().filter(j -> "blocked".equals(j.getStatus())).count();
            long notStarted = all.stream().filter(j -> "not-started".equals(j.getStatus())).count();

            // Calculate average progress
            double avgProgress = all.stream()
                    .mapToInt(JourneyMap::getProgress)
                    .average()
                    .orElse(0);

            // Count journeys with PDF
            long withPdf = all.stream()
                    .filter(j -> j.getPdfData() != null && !j.getPdfData().isEmpty())
                    .count();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalJourneys", total);
            stats.put("completed", completed);
            stats.put("inProgress", inProgress);
            stats.put("blocked", blocked);
            stats.put("notStarted", notStarted);
            stats.put("averageProgress", Math.round(avgProgress));
            stats.put("withPdf", withPdf);

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error fetching stats: " + e.getMessage()));
        }
    }

    // =====================================
    // UPDATE JOURNEY STATUS
    // =====================================
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> statusUpdate) {
        return journeyMapRepository.findById(id)
                .map(existing -> {
                    String newStatus = statusUpdate.get("status");
                    if (newStatus == null || newStatus.isEmpty()) {
                        return ResponseEntity.badRequest().body("Status is required");
                    }

                    existing.setStatus(newStatus);
                    existing.setUpdatedDate(LocalDateTime.now());

                    if ("completed".equals(newStatus)) {
                        existing.setProgress(100);
                    }

                    JourneyMap updated = journeyMapRepository.save(existing);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // =====================================
    // EXPORT JOURNEY AS JSON
    // =====================================
    @GetMapping("/export/{id}")
    public ResponseEntity<?> exportJourney(@PathVariable Long id) {
        return journeyMapRepository.findById(id)
                .map(journey -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        String json = mapper.writerWithDefaultPrettyPrinter()
                                .writeValueAsString(journey);

                        byte[] data = json.getBytes(StandardCharsets.UTF_8);
                        
                        return ResponseEntity.ok()
                                .header("Content-Disposition", 
                                        "attachment; filename=\"" + journey.getName().replace(" ", "_") + "_journey.json\"")
                                .header("Content-Type", "application/json")
                                .body(data);

                    } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Error exporting journey: " + e.getMessage());
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // =====================================
    // OPEN JOURNEY IN VS CODE
    // =====================================
    @GetMapping("/open-vscode/{id}")
    public ResponseEntity<?> openInVSCode(@PathVariable Long id) {
        return journeyMapRepository.findById(id)
                .map(journey -> {
                    try {
                        String tempDir = System.getProperty("java.io.tmpdir");
                        String fileName = journey.getName().replace(" ", "_") + "_journey.json";
                        
                        Path tempFile = Paths.get(tempDir, fileName);
                        
                        ObjectMapper mapper = new ObjectMapper();
                        Map<String, Object> exportData = new HashMap<>();
                        exportData.put("id", journey.getId());
                        exportData.put("name", journey.getName());
                        exportData.put("module", journey.getModule());
                        exportData.put("description", journey.getDescription());
                        exportData.put("status", journey.getStatus());
                        exportData.put("progress", journey.getProgress());
                        exportData.put("sheetLink", journey.getSheetLink());
                        exportData.put("pdfLink", journey.getPdfLink());
                        exportData.put("pdfFileName", journey.getPdfFileName());
                        exportData.put("stages", mapper.readValue(journey.getStagesJson(), Object.class));
                        exportData.put("createdDate", journey.getCreatedDate());
                        exportData.put("updatedDate", journey.getUpdatedDate());
                        exportData.put("estimatedCompletion", journey.getEstimatedCompletion());

                        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(exportData);
                        Files.write(tempFile, json.getBytes(StandardCharsets.UTF_8));
                        
                        String vscodePath = "\"C:\\Users\\Rahul Kumar\\AppData\\Local\\Programs\\Microsoft VS Code\\Code.exe\"";
                        String command = vscodePath + " \"" + tempFile.toString() + "\"";
                        
                        Process process = Runtime.getRuntime().exec(command);
                        
                        return ResponseEntity.ok(Map.of(
                            "message", "Journey opened in VS Code",
                            "file", tempFile.toString(),
                            "fileName", fileName
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

    // =====================================
    // HELPER: Calculate Progress
    // =====================================
    private int calculateProgress(String stagesJson) {
        if (stagesJson == null || stagesJson.isEmpty()) {
            return 0;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> stages = mapper.readValue(stagesJson, List.class);
            
            if (stages == null || stages.isEmpty()) {
                return 0;
            }

            long total = stages.size();
            long completed = stages.stream()
                    .filter(stage -> {
                        Boolean isCompleted = (Boolean) stage.get("completed");
                        return isCompleted != null && isCompleted;
                    })
                    .count();

            return (int) ((completed * 100) / total);

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}