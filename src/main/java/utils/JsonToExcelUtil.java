package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class JsonToExcelUtil {

    // Language display names mapping
    private static final Map<String, String> LANGUAGE_DISPLAY_NAMES = new HashMap<>();

    static {
        // Indian Languages
        LANGUAGE_DISPLAY_NAMES.put("text_en", "English 🇬🇧");
        LANGUAGE_DISPLAY_NAMES.put("text_hi", "Hindi 🇮🇳");
        LANGUAGE_DISPLAY_NAMES.put("text_ml", "Malayalam 🇮🇳");
        LANGUAGE_DISPLAY_NAMES.put("text_ta", "Tamil 🇮🇳");
        LANGUAGE_DISPLAY_NAMES.put("text_te", "Telugu 🇮🇳");
        LANGUAGE_DISPLAY_NAMES.put("text_bn", "Bengali 🇮🇳");
        LANGUAGE_DISPLAY_NAMES.put("text_kn", "Kannada 🇮🇳");
        LANGUAGE_DISPLAY_NAMES.put("text_mr", "Marathi 🇮🇳");
        LANGUAGE_DISPLAY_NAMES.put("text_gu", "Gujarati 🇮🇳");
        LANGUAGE_DISPLAY_NAMES.put("text_or", "Odia 🇮🇳");
        LANGUAGE_DISPLAY_NAMES.put("text_pa", "Punjabi 🇮🇳");
        LANGUAGE_DISPLAY_NAMES.put("text_ur", "Urdu 🇮🇳");

        // International Languages
        LANGUAGE_DISPLAY_NAMES.put("text_ar", "Arabic 🇸🇦");
        LANGUAGE_DISPLAY_NAMES.put("text_fr", "French 🇫🇷");
        LANGUAGE_DISPLAY_NAMES.put("text_de", "German 🇩🇪");
        LANGUAGE_DISPLAY_NAMES.put("text_es", "Spanish 🇪🇸");
        LANGUAGE_DISPLAY_NAMES.put("text_pt", "Portuguese 🇵🇹");
        LANGUAGE_DISPLAY_NAMES.put("text_ru", "Russian 🇷🇺");
        LANGUAGE_DISPLAY_NAMES.put("text_zh", "Chinese 🇨🇳");
        LANGUAGE_DISPLAY_NAMES.put("text_ja", "Japanese 🇯🇵");
        LANGUAGE_DISPLAY_NAMES.put("text_ko", "Korean 🇰🇷");
        LANGUAGE_DISPLAY_NAMES.put("text_it", "Italian 🇮🇹");
        LANGUAGE_DISPLAY_NAMES.put("text_nl", "Dutch 🇳🇱");
        LANGUAGE_DISPLAY_NAMES.put("text_tr", "Turkish 🇹🇷");
        LANGUAGE_DISPLAY_NAMES.put("text_vi", "Vietnamese 🇻🇳");
        LANGUAGE_DISPLAY_NAMES.put("text_th", "Thai 🇹🇭");
    }

    /**
     * Get language display name with flag emoji
     */
    public static String getLanguageDisplayName(String field) {
        return LANGUAGE_DISPLAY_NAMES.getOrDefault(field, field + " 🌐");
    }

    /**
     * Detect all language fields from JSON
     */
    public static List<String> detectLanguageFields(String jsonData) {
        try {
            System.out.println("🔍 detectLanguageFields() called");
            System.out.println("📊 JSON length: " + jsonData.length());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonData);

            System.out.println("📊 Root type: " + root.getNodeType());

            if (!root.isArray()) {
                System.out.println("❌ Root is NOT an array! Returning empty list.");
                return new ArrayList<>();
            }

            System.out.println("✅ JSON array has " + root.size() + " items");

            if (root.size() == 0) {
                System.out.println("⚠️ JSON array is empty!");
                return new ArrayList<>();
            }

            JsonNode first = root.get(0);
            List<String> fields = new ArrayList<>();
            first.fieldNames().forEachRemaining(fields::add);

            System.out.println("📋 All fields: " + fields);

            // Filter only text_* fields
            List<String> languageFields = fields.stream()
                    .filter(f -> f.startsWith("text_"))
                    .sorted()
                    .toList();

            System.out.println("🌐 Detected language fields: " + languageFields);

            return languageFields;

        } catch (Exception e) {
            System.err.println("❌ Error detecting language fields: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Generate Excel from JSON with selected languages
     */
    public static String generateExcel(String jsonData, String fileName, List<String> selectedLanguages, String outputDir) {
        try {
            System.out.println("📊 generateExcel() called");
            System.out.println("   File name: " + fileName);
            System.out.println("   Selected languages: " + selectedLanguages);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonData);

            if (!root.isArray()) {
                throw new IllegalArgumentException("JSON must be an array");
            }

            if (selectedLanguages == null || selectedLanguages.isEmpty()) {
                throw new IllegalArgumentException("Please select at least one language");
            }

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Responses");

            // Header row with styling
            Row header = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (int i = 0; i < selectedLanguages.size(); i++) {
                String lang = selectedLanguages.get(i);
                String displayName = getLanguageDisplayName(lang);
                Cell cell = header.createCell(i);
                cell.setCellValue(displayName);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowNum = 1;
            for (JsonNode node : root) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < selectedLanguages.size(); i++) {
                    String lang = selectedLanguages.get(i);
                    String value = node.has(lang) ? node.get(lang).asText() : "";
                    row.createCell(i).setCellValue(value);
                }
            }

            // Auto-size columns
            for (int i = 0; i < selectedLanguages.size(); i++) {
                sheet.autoSizeColumn(i);
                if (sheet.getColumnWidth(i) < 3000) {
                    sheet.setColumnWidth(i, 3000);
                }
            }

            // Create output directory
            File dir = new File(outputDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Generate filename with timestamp
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String excelPath = outputDir + "/" + fileName + "_" + timestamp + ".xlsx";

            // Write to file
            FileOutputStream fos = new FileOutputStream(excelPath);
            workbook.write(fos);
            fos.close();
            workbook.close();

            System.out.println("✅ Excel Generated: " + excelPath);
            System.out.println("   Languages: " + selectedLanguages);
            System.out.println("   Records: " + root.size());

            return excelPath;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating Excel: " + e.getMessage());
        }
    }

    /**
     * Generate Excel from JSON file path (Legacy support)
     */
    public static void generateExcel(String jsonPath, String excelPath, List<String> selectedLanguages) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(new File(jsonPath));
            String jsonData = mapper.writeValueAsString(root);

            String fileName = new File(excelPath).getName().replace(".xlsx", "");
            String outputDir = new File(excelPath).getParent();
            if (outputDir == null) {
                outputDir = "Reports";
            }

            generateExcel(jsonData, fileName, selectedLanguages, outputDir);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating Excel: " + e.getMessage());
        }
    }

    /**
     * Original method - keeps backward compatibility
     */
    public static void generateExcel(String jsonPath, String excelPath) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(new File(jsonPath));
            String jsonData = mapper.writeValueAsString(root);

            List<String> allLanguages = detectLanguageFields(jsonData);
            if (allLanguages.isEmpty()) {
                throw new IllegalArgumentException("No language fields (text_*) found in JSON");
            }

            String fileName = new File(excelPath).getName().replace(".xlsx", "");
            String outputDir = new File(excelPath).getParent();
            if (outputDir == null) {
                outputDir = "Reports";
            }

            generateExcel(jsonData, fileName, allLanguages, outputDir);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating Excel: " + e.getMessage());
        }
    }
}