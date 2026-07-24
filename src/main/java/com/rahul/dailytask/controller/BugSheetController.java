package com.rahul.dailytask.controller;

import com.rahul.dailytask.entity.BugSheet;
import com.rahul.dailytask.repository.BugSheetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/bugsheets")
@CrossOrigin(origins = "*")
public class BugSheetController {

    @Autowired
    private BugSheetRepository bugSheetRepository;

    // GET all bug sheets
    @GetMapping
    public ResponseEntity<List<BugSheet>> getAllBugSheets() {
        List<BugSheet> sheets = bugSheetRepository.findByOrderByCreatedDateDesc();
        return ResponseEntity.ok(sheets);
    }

    // GET bug sheets by hospital name
    @GetMapping("/hospital/{hospitalName}")
    public ResponseEntity<List<BugSheet>> getBugSheetsByHospital(@PathVariable String hospitalName) {
        List<BugSheet> sheets = bugSheetRepository.findByHospitalNameOrderByCreatedDateDesc(hospitalName);
        return ResponseEntity.ok(sheets);
    }

    // POST create bug sheet
    @PostMapping
    public ResponseEntity<?> createBugSheet(@RequestBody BugSheet bugSheet) {
        try {
            // Set all required fields
            if (bugSheet.getCreatedDate() == null) {
                bugSheet.setCreatedDate(LocalDateTime.now());
            }
            if (bugSheet.getUploadDate() == null) {
                bugSheet.setUploadDate(LocalDateTime.now());
            }
            if (bugSheet.getOriginalFileName() == null || bugSheet.getOriginalFileName().isEmpty()) {
                bugSheet.setOriginalFileName("Google Drive Sheet");
            }
            if (bugSheet.getStoredFileName() == null || bugSheet.getStoredFileName().isEmpty()) {
                bugSheet.setStoredFileName("gdrive_" + System.currentTimeMillis());
            }
            if (bugSheet.getSheetName() == null || bugSheet.getSheetName().isEmpty()) {
                bugSheet.setSheetName(bugSheet.getTestingModule());
            }
            if (bugSheet.getTotalBugs() == null) {
                bugSheet.setTotalBugs(0);
            }
            if (bugSheet.getUploadedBy() == null || bugSheet.getUploadedBy().isEmpty()) {
                bugSheet.setUploadedBy("System");
            }
            if (bugSheet.getFileName() == null || bugSheet.getFileName().isEmpty()) {
                bugSheet.setFileName(bugSheet.getGoogleDriveLink());
            }

            BugSheet saved = bugSheetRepository.save(bugSheet);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving bug sheet: " + e.getMessage());
        }
    }

    // DELETE bug sheet by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBugSheet(@PathVariable Long id) {
        if (!bugSheetRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        bugSheetRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}