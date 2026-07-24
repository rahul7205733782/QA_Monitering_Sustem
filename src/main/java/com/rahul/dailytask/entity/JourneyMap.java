package com.rahul.dailytask.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "journey_maps")
public class JourneyMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String module;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String stagesJson;  // Made nullable

    @Column(nullable = false)
    private String status = "not-started";

    private Integer progress = 0;

    private LocalDateTime estimatedCompletion;

    @Column(nullable = false)
    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    // ===== NEW PDF FIELDS =====
    @Column(name = "sheet_link", columnDefinition = "TEXT")
    private String sheetLink;

    @Column(name = "pdf_link", columnDefinition = "TEXT")
    private String pdfLink;

    @Column(name = "pdf_data", columnDefinition = "LONGTEXT")
    private String pdfData;

    @Column(name = "pdf_file_name")
    private String pdfFileName;

    @Column(name = "created_by")
    private String createdBy;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStagesJson() {
        return stagesJson;
    }

    public void setStagesJson(String stagesJson) {
        this.stagesJson = stagesJson;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public LocalDateTime getEstimatedCompletion() {
        return estimatedCompletion;
    }

    public void setEstimatedCompletion(LocalDateTime estimatedCompletion) {
        this.estimatedCompletion = estimatedCompletion;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    // ===== NEW GETTERS AND SETTERS =====
    public String getSheetLink() {
        return sheetLink;
    }

    public void setSheetLink(String sheetLink) {
        this.sheetLink = sheetLink;
    }

    public String getPdfLink() {
        return pdfLink;
    }

    public void setPdfLink(String pdfLink) {
        this.pdfLink = pdfLink;
    }

    public String getPdfData() {
        return pdfData;
    }

    public void setPdfData(String pdfData) {
        this.pdfData = pdfData;
    }

    public String getPdfFileName() {
        return pdfFileName;
    }

    public void setPdfFileName(String pdfFileName) {
        this.pdfFileName = pdfFileName;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public String toString() {
        return "JourneyMap{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", module='" + module + '\'' +
                ", sheetLink='" + sheetLink + '\'' +
                ", pdfLink='" + pdfLink + '\'' +
                ", pdfData=" + (pdfData != null ? "present (" + pdfData.length() + " chars)" : "null") +
                ", pdfFileName='" + pdfFileName + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}