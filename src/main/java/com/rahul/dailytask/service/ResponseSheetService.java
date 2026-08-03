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

    // =====================================
    // GENERATE EXCEL - UPDATED SIGNATURE
    // =====================================
    public ResponseSheetHistory generateExcel(String jsonData, String fileName, List<String> selectedLanguages, List<String> languageHeaders, String uploadedBy) {
        try {
            System.out.println("📊 generateExcel() service called");
            System.out.println("   File name: " + fileName);
            System.out.println("   Languages: " + selectedLanguages);
            System.out.println("   Headers:   " + languageHeaders);

            int recordCount = getRecordCountFromJson(jsonData);
            System.out.println("📊 Record count: " + recordCount);

            // Generate Excel file - PASS languageHeaders to util
            String excelPath = JsonToExcelUtil.generateExcel(jsonData, fileName, selectedLanguages, languageHeaders, "Reports");

            ResponseSheetHistory history = new ResponseSheetHistory();
            history.setFileName(new java.io.File(excelPath).getName());
            history.setUploadedBy(uploadedBy);
            history.setGeneratedDate(LocalDateTime.now());
            history.setDownloadCount(0);
            history.setSelectedLanguages(String.join(", ", languageHeaders)); // Save display names, not raw keys
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

    // =====================================
    // GET HISTORY
    // =====================================
    public List<ResponseSheetHistory> getHistory() {
        return historyRepository.findAllByOrderByGeneratedDateDesc();
    }

    public List<ResponseSheetHistory> getHistoryByUser(String uploadedBy) {
        return historyRepository.findByUploadedBy(uploadedBy);
    }

    // =====================================
    // INCREMENT DOWNLOAD COUNT
    // =====================================
    public void incrementDownloadCount(Long historyId) {
        historyRepository.findById(historyId).ifPresent(history -> {
            history.setDownloadCount(history.getDownloadCount() + 1);
            historyRepository.save(history);
            System.out.println("📥 Download count incremented for ID: " + historyId + " (Total: " + history.getDownloadCount() + ")");
        });
    }

    // =====================================
    // GET FILE PATH
    // =====================================
    public String getFilePath(Long historyId) {
        return historyRepository.findById(historyId)
                .map(ResponseSheetHistory::getFilePath)
                .orElse(null);
    }

    // =====================================
    // DETECT LANGUAGES
    // =====================================
    public List<String> detectLanguages(String jsonData) {
        System.out.println("🔍 detectLanguages() service called");
        List<String> languages = JsonToExcelUtil.detectLanguageFields(jsonData);
        System.out.println("🔍 Returning languages: " + languages);
        return languages;
    }

    // =====================================
    // DELETE METHODS
    // =====================================
    public boolean deleteHistory(Long id) {
        if (historyRepository.existsById(id)) {
            historyRepository.deleteById(id);
            System.out.println("🗑️ History record deleted from database: " + id);
            return true;
        }
        System.out.println("❌ History record not found: " + id);
        return false;
    }

    public void clearAllHistory() {
        long count = historyRepository.count();
        historyRepository.deleteAll();
        System.out.println("🗑️ Cleared " + count + " history records from database");
    }

    // =====================================
    // HELPER: GET RECORD COUNT
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