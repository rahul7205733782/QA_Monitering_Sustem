package com.rahul.dailytask.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tasks")
public class Task {

    // =========================
    // PRIMARY KEY
    // =========================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================
    // BASIC DETAILS
    // =========================

    @Column(name = "date")
    private String testDate;

    @Column(name = "client_name")
    private String clientName;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "hospital_name")
    private String hospitalName;

    @Column(name = "employee_name")
    private String employeeName;

    @Column(name = "developer_name")
    private String devName;

    @Column(name = "environment")
    private String environment;

    // =========================
    // TASK DETAILS
    // =========================

    @Column(name = "task_title")
    private String taskTitle;

    @Column(name = "task_type")
    private String taskType;

    @Column(name = "module_name")
    private String moduleName;

    @Column(name = "testing_type")
    private String testingType;

    @Column(name = "scenario_tested", columnDefinition = "TEXT")
    private String scenario;
    
    // =========================
    // STATUS
    // =========================

    @Column(name = "status")
    private String status;

    @Column(name = "uat_status")
    private String uatStatus;

    @Column(name = "prod_status")
    private String prodStatus;

    @Column(name = "priority")
    private String priority;

    @Column(name = "severity")
    private String severity;

    // =========================
    // TEST DETAILS
    // =========================

    @Column(name = "browser")
    private String browser;

    @Column(name = "device_name")
    private String deviceName;

    @Column(name = "tested_number")
    private String testedNumber;

    @Column(name = "tested_url", columnDefinition = "TEXT")
    private String testedUrl;

    // =========================
    // TIME
    // =========================

    @Column(name = "created_date")
    private String createdDate;

    @Column(name = "start_time")
    private String startTime;

    @Column(name = "end_time")
    private String endTime;

    // =========================
    // RESULTS
    // =========================

    @Column(name = "expected_result", columnDefinition = "TEXT")
    private String expectedResult;

    @Column(name = "actual_result", columnDefinition = "TEXT")
    private String actualResult;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    // =========================
    // BUG DETAILS
    // =========================

    @Column(name = "bug_id")
    private String bugId;

    // =========================
    // SCREENSHOT
    // =========================

    @Column(name = "screenshot")
    private String screenshot;

    // =========================
    // GETTERS & SETTERS - FIXED
    // =========================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // FIXED: Now uses correct getter/setter names that match the field
    public String getTestDate() {
        return testDate;
    }

    public void setTestDate(String testDate) {
        this.testDate = testDate;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    // FIXED: Now uses correct getter/setter names that match the field
    public String getDevName() {
        return devName;
    }

    public void setDevName(String devName) {
        this.devName = devName;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getTestingType() {
        return testingType;
    }

    public void setTestingType(String testingType) {
        this.testingType = testingType;
    }

    // FIXED: Now uses correct getter/setter names that match the field
    public String getScenario() {
        return scenario;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUatStatus() {
        return uatStatus;
    }

    public void setUatStatus(String uatStatus) {
        this.uatStatus = uatStatus;
    }

    public String getProdStatus() {
        return prodStatus;
    }

    public void setProdStatus(String prodStatus) {
        this.prodStatus = prodStatus;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getTestedNumber() {
        return testedNumber;
    }

    public void setTestedNumber(String testedNumber) {
        this.testedNumber = testedNumber;
    }

    public String getTestedUrl() {
        return testedUrl;
    }

    public void setTestedUrl(String testedUrl) {
        this.testedUrl = testedUrl;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getExpectedResult() {
        return expectedResult;
    }

    public void setExpectedResult(String expectedResult) {
        this.expectedResult = expectedResult;
    }

    public String getActualResult() {
        return actualResult;
    }

    public void setActualResult(String actualResult) {
        this.actualResult = actualResult;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getBugId() {
        return bugId;
    }

    public void setBugId(String bugId) {
        this.bugId = bugId;
    }

    public String getScreenshot() {
        return screenshot;
    }

    public void setScreenshot(String screenshot) {
        this.screenshot = screenshot;
    }
}