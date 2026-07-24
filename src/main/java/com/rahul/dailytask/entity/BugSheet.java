package com.rahul.dailytask.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

@Entity
@Table(name = "bug_sheets")
public class BugSheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hospital_name")
    private String hospitalName;

    @Column(name = "testing_module", nullable = false)
    private String testingModule;

    @Column(name = "google_drive_link", nullable = false, length = 1000)
    private String googleDriveLink;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "sheet_name")
    private String sheetName;

    @Column(name = "total_bugs")
    private Integer totalBugs;

    @Column(name = "upload_date")
    private LocalDateTime uploadDate;

    @Column(name = "upload_time")
    private LocalTime uploadTime;

    @Column(name = "uploaded_by")
    private String uploadedBy;

    @Column(name = "original_file_name")
    private String originalFileName;

    @Column(name = "stored_file_name")
    private String storedFileName;

    @Column(name = "hospital_id")
    private Long hospitalId;

    public BugSheet() {
        this.createdDate = LocalDateTime.now();
        this.uploadDate = LocalDateTime.now();
    }

    public BugSheet(String hospitalName, String testingModule, String googleDriveLink) {
        this.hospitalName = hospitalName;
        this.testingModule = testingModule;
        this.googleDriveLink = googleDriveLink;
        this.createdDate = LocalDateTime.now();
        this.uploadDate = LocalDateTime.now();
        this.originalFileName = "Google Drive Sheet";
        this.storedFileName = "gdrive_" + System.currentTimeMillis();
        this.sheetName = testingModule;
        this.totalBugs = 0;
        this.uploadedBy = "System";
        this.fileName = googleDriveLink;
        this.filePath = "";
        this.uploadTime = LocalTime.now();
    }

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

    public String getTestingModule() {
        return testingModule;
    }

    public void setTestingModule(String testingModule) {
        this.testingModule = testingModule;
    }

    public String getGoogleDriveLink() {
        return googleDriveLink;
    }

    public void setGoogleDriveLink(String googleDriveLink) {
        this.googleDriveLink = googleDriveLink;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public Integer getTotalBugs() {
        return totalBugs;
    }

    public void setTotalBugs(Integer totalBugs) {
        this.totalBugs = totalBugs;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public LocalTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(LocalTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public void setStoredFileName(String storedFileName) {
        this.storedFileName = storedFileName;
    }

    public Long getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(Long hospitalId) {
        this.hospitalId = hospitalId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BugSheet)) return false;
        BugSheet bugSheet = (BugSheet) o;
        return Objects.equals(id, bugSheet.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}