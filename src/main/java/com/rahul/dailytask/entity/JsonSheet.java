package com.rahul.dailytask.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "json_sheets")
public class JsonSheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "module_name", nullable = false)
    private String moduleName;

    @Column(name = "json_file_name", nullable = false)
    private String jsonFileName;

    @Column(name = "json_content", columnDefinition = "LONGTEXT", nullable = false)
    private String jsonContent;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "file_size")
    private Long fileSize;

    // Constructors
    public JsonSheet() {
        this.createdDate = LocalDateTime.now();
    }

    public JsonSheet(String moduleName, String jsonFileName, String jsonContent) {
        this.moduleName = moduleName;
        this.jsonFileName = jsonFileName;
        this.jsonContent = jsonContent;
        this.createdDate = LocalDateTime.now();
        this.fileSize = (long) jsonContent.length();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }

    public String getJsonFileName() { return jsonFileName; }
    public void setJsonFileName(String jsonFileName) { this.jsonFileName = jsonFileName; }

    public String getJsonContent() { return jsonContent; }
    public void setJsonContent(String jsonContent) { this.jsonContent = jsonContent; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonSheet jsonSheet = (JsonSheet) o;
        return Objects.equals(id, jsonSheet.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "JsonSheet{" +
                "id=" + id +
                ", moduleName='" + moduleName + '\'' +
                ", jsonFileName='" + jsonFileName + '\'' +
                ", createdDate=" + createdDate +
                '}';
    }
}