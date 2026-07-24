package com.rahul.dailytask.controller;

import com.rahul.dailytask.entity.ApiDoc;
import com.rahul.dailytask.service.ApiDocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/api-docs")
@CrossOrigin(origins = "*")
public class ApiDocController {

    @Autowired
    private ApiDocService apiDocService;

    // =========================================
    // GET ALL DOCS
    // =========================================
    @GetMapping
    public ResponseEntity<List<ApiDoc>> getAllDocs() {
        try {
            List<ApiDoc> docs = apiDocService.getAllDocs();
            System.out.println("📤 Returning " + docs.size() + " API docs");
            return ResponseEntity.ok(docs);
        } catch (Exception e) {
            System.err.println("❌ Error getting docs: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // =========================================
    // GET DOC BY ID
    // =========================================
    @GetMapping("/{id}")
    public ResponseEntity<?> getDocById(@PathVariable Long id) {
        try {
            Optional<ApiDoc> doc = apiDocService.getDocById(id);
            if (doc.isPresent()) {
                return ResponseEntity.ok(doc.get());
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Doc not found with id: " + id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting doc: " + e.getMessage());
        }
    }

    // =========================================
    // GET DOCS BY HOSPITAL NAME
    // =========================================
    @GetMapping("/search/hospital")
    public ResponseEntity<List<ApiDoc>> getDocsByHospital(@RequestParam String hospitalName) {
        try {
            List<ApiDoc> docs = apiDocService.getDocsByHospitalName(hospitalName);
            return ResponseEntity.ok(docs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // =========================================
    // GET DOCS BY TEMPLATE TYPE
    // =========================================
    @GetMapping("/search/template")
    public ResponseEntity<List<ApiDoc>> getDocsByTemplate(@RequestParam String templateType) {
        try {
            List<ApiDoc> docs = apiDocService.getDocsByTemplateType(templateType);
            return ResponseEntity.ok(docs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // =========================================
    // GET STATISTICS
    // =========================================
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalDocs", apiDocService.getTotalDocs());
            stats.put("templateTypes", apiDocService.getAllTemplateTypes());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // =========================================
    // SAVE NEW DOC
    // =========================================
    @PostMapping
    public ResponseEntity<?> saveDoc(@RequestBody ApiDoc doc) {
        try {
            System.out.println("📥 POST request received");
            System.out.println("📝 Doc data: " + doc);

            // Validate
            if (doc.getHospitalName() == null || doc.getHospitalName().isEmpty()) {
                return ResponseEntity.badRequest().body("Hospital name is required");
            }
            if (doc.getTemplateType() == null || doc.getTemplateType().isEmpty()) {
                return ResponseEntity.badRequest().body("Template type is required");
            }
            if (doc.getSheetLink() == null || doc.getSheetLink().isEmpty()) {
                return ResponseEntity.badRequest().body("Sheet link is required");
            }

            ApiDoc saved = apiDocService.saveDoc(doc);
            System.out.println("✅ Doc saved with ID: " + saved.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            System.err.println("❌ Error saving doc: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving doc: " + e.getMessage());
        }
    }

    // =========================================
    // UPDATE DOC
    // =========================================
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDoc(@PathVariable Long id, @RequestBody ApiDoc doc) {
        try {
            System.out.println("📥 PUT request for ID: " + id);
            System.out.println("📝 Doc data: " + doc);

            ApiDoc updated = apiDocService.updateDoc(id, doc);
            System.out.println("✅ Doc updated: " + updated);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            System.err.println("❌ Doc not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Doc not found with id: " + id);
        } catch (Exception e) {
            System.err.println("❌ Error updating doc: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating doc: " + e.getMessage());
        }
    }

    // =========================================
    // DELETE DOC
    // =========================================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDoc(@PathVariable Long id) {
        try {
            apiDocService.deleteDoc(id);
            System.out.println("🗑️ Doc deleted with ID: " + id);
            return ResponseEntity.ok("Doc deleted successfully");
        } catch (RuntimeException e) {
            System.err.println("❌ Doc not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Doc not found with id: " + id);
        } catch (Exception e) {
            System.err.println("❌ Error deleting doc: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting doc: " + e.getMessage());
        }
    }
}