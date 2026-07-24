package com.rahul.dailytask.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "response_sheet_history")
public class ResponseSheetHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "uploaded_by")
    private String uploadedBy;

    @Column(name = "generated_date")
    private LocalDateTime generatedDate;

    @Column(name = "download_count")
    private Integer downloadCount = 0;

    @Column(name = "selected_languages", columnDefinition = "TEXT")
    private String selectedLanguages;

    @Column(name = "total_records")
    private Integer totalRecords;

    @Column(name = "file_path")
    private String filePath;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public LocalDateTime getGeneratedDate() {
        return generatedDate;
    }

    public void setGeneratedDate(LocalDateTime generatedDate) {
        this.generatedDate = generatedDate;
    }

    public Integer getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }

    public String getSelectedLanguages() {
        return selectedLanguages;
    }

    public void setSelectedLanguages(String selectedLanguages) {
        this.selectedLanguages = selectedLanguages;
    }

    public Integer getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(Integer totalRecords) {
        this.totalRecords = totalRecords;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}