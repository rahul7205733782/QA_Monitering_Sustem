package com.rahul.dailytask.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rahul.dailytask.entity.ResponseSheetHistory;
import com.rahul.dailytask.repository.ResponseSheetHistoryRepository;
import utils.JsonToExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ResponseSheetService {

    @Autowired
    private ResponseSheetHistoryRepository historyRepository;

    /**
     * Generate Excel from JSON data
     */
    public ResponseSheetHistory generateExcel(String jsonData, String fileName, List<String> selectedLanguages, String uploadedBy) {
        try {
            System.out.println("📊 generateExcel() service called");
            System.out.println("   File name: " + fileName);
            System.out.println("   Languages: " + selectedLanguages);

            // Get record count properly
            int recordCount = getRecordCountFromJson(jsonData);
            System.out.println("📊 Record count: " + recordCount);

            // Generate Excel file
            String excelPath = JsonToExcelUtil.generateExcel(jsonData, fileName, selectedLanguages, "Reports");

            // Create history record
            ResponseSheetHistory history = new ResponseSheetHistory();
            history.setFileName(new java.io.File(excelPath).getName());
            history.setUploadedBy(uploadedBy);
            history.setGeneratedDate(LocalDateTime.now());
            history.setDownloadCount(0);
            history.setSelectedLanguages(String.join(", ", selectedLanguages));
            history.setTotalRecords(recordCount);
            history.setFilePath(excelPath);

            ResponseSheetHistory saved = historyRepository.save(history);
            System.out.println("✅ History saved with ID: " + saved.getId());

            return saved;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating Excel: " + e.getMessage());
        }
    }

    /**
     * Get all history records
     */
    public List<ResponseSheetHistory> getHistory() {
        return historyRepository.findAllByOrderByGeneratedDateDesc();
    }

    /**
     * Get history records by user
     */
    public List<ResponseSheetHistory> getHistoryByUser(String uploadedBy) {
        return historyRepository.findByUploadedBy(uploadedBy);
    }

    /**
     * Increment download count
     */
    public void incrementDownloadCount(Long historyId) {
        historyRepository.findById(historyId).ifPresent(history -> {
            history.setDownloadCount(history.getDownloadCount() + 1);
            historyRepository.save(history);
            System.out.println("📥 Download count incremented for ID: " + historyId + " (Total: " + history.getDownloadCount() + ")");
        });
    }

    /**
     * Get file path by history ID
     */
    public String getFilePath(Long historyId) {
        return historyRepository.findById(historyId)
                .map(ResponseSheetHistory::getFilePath)
                .orElse(null);
    }

    /**
     * Detect languages from JSON
     */
    public List<String> detectLanguages(String jsonData) {
        System.out.println("🔍 detectLanguages() service called");
        List<String> languages = JsonToExcelUtil.detectLanguageFields(jsonData);
        System.out.println("🔍 Returning languages: " + languages);
        return languages;
    }

    // =====================================
    // DELETE METHODS
    // =====================================

    /**
     * Delete history by ID
     */
    public boolean deleteHistory(Long id) {
        if (historyRepository.existsById(id)) {
            historyRepository.deleteById(id);
            System.out.println("🗑️ History record deleted from database: " + id);
            return true;
        }
        System.out.println("❌ History record not found: " + id);
        return false;
    }

    /**
     * Clear all history
     */
    public void clearAllHistory() {
        long count = historyRepository.count();
        historyRepository.deleteAll();
        System.out.println("🗑️ Cleared " + count + " history records from database");
    }

    // =====================================
    // HELPER: Get record count from JSON
    // =====================================
    private int getRecordCountFromJson(String jsonData) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonData);

            if (!root.isArray()) {
                return 0;
            }

            return root.size();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}