package com.rahul.dailytask.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "api_docs")
public class ApiDoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hospital_name", nullable = false, length = 200)
    private String hospitalName;

    @Column(name = "template_type", nullable = false, length = 100)
    private String templateType;

    @Column(name = "sheet_link", nullable = false, length = 500)
    private String sheetLink;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    // =========================================
    // CONSTRUCTORS
    // =========================================

    public ApiDoc() {
        this.createdDate = LocalDateTime.now();
    }

    public ApiDoc(String hospitalName, String templateType, String sheetLink) {
        this.hospitalName = hospitalName;
        this.templateType = templateType;
        this.sheetLink = sheetLink;
        this.createdDate = LocalDateTime.now();
    }

    // =========================================
    // GETTERS AND SETTERS
    // =========================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public String getSheetLink() {
        return sheetLink;
    }

    public void setSheetLink(String sheetLink) {
        this.sheetLink = sheetLink;
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

    @Override
    public String toString() {
        return "ApiDoc{" +
                "id=" + id +
                ", hospitalName='" + hospitalName + '\'' +
                ", templateType='" + templateType + '\'' +
                ", sheetLink='" + sheetLink + '\'' +
                ", createdDate=" + createdDate +
                '}';
    }
}