package com.rahul.dailytask.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dashboard_monitor")
public class DashboardMonitor {

    // =========================================
    // STATUS CONSTANTS
    // =========================================
    public static final String STATUS_UP = "UP";
    public static final String STATUS_DOWN = "DOWN";
    public static final String STATUS_CHECKING = "CHECKING";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_name", nullable = false, length = 100)
    private String clientName;

    @Column(name = "dashboard_url", nullable = false, length = 500)
    private String dashboardUrl;

    @Column(name = "status", length = 20)
    private String status = STATUS_CHECKING;

    @Column(name = "status_code")
    private Integer statusCode = 0;

    @Column(name = "response_time")
    private Long responseTime = 0L;

    @Column(name = "last_checked")
    private LocalDateTime lastChecked;

    @Column(name = "remarks", length = 500)
    private String remarks = "";

    @Column(name = "active")
    private Boolean active = true;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "uptime_percentage")
    private Double uptimePercentage = 100.0;

    @Column(name = "total_checks")
    private Long totalChecks = 0L;

    @Column(name = "successful_checks")
    private Long successfulChecks = 0L;

    // =========================================
    // CONSTRUCTORS
    // =========================================

    public DashboardMonitor() {
        this.active = true;
        this.createdDate = LocalDateTime.now();
        this.totalChecks = 0L;
        this.successfulChecks = 0L;
        this.uptimePercentage = 100.0;
        this.status = STATUS_CHECKING;
        this.statusCode = 0;
        this.responseTime = 0L;
        this.remarks = "";
    }

    public DashboardMonitor(String clientName, String dashboardUrl) {
        this.clientName = clientName;
        this.dashboardUrl = dashboardUrl;
        this.active = true;
        this.createdDate = LocalDateTime.now();
        this.totalChecks = 0L;
        this.successfulChecks = 0L;
        this.uptimePercentage = 100.0;
        this.status = STATUS_CHECKING;
        this.statusCode = 0;
        this.responseTime = 0L;
        this.remarks = "";
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

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getDashboardUrl() {
        return dashboardUrl;
    }

    public void setDashboardUrl(String dashboardUrl) {
        this.dashboardUrl = dashboardUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public Long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Long responseTime) {
        this.responseTime = responseTime;
    }

    public LocalDateTime getLastChecked() {
        return lastChecked;
    }

    public void setLastChecked(LocalDateTime lastChecked) {
        this.lastChecked = lastChecked;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public Double getUptimePercentage() {
        return uptimePercentage;
    }

    public void setUptimePercentage(Double uptimePercentage) {
        this.uptimePercentage = uptimePercentage;
    }

    public Long getTotalChecks() {
        return totalChecks;
    }

    public void setTotalChecks(Long totalChecks) {
        this.totalChecks = totalChecks;
    }

    public Long getSuccessfulChecks() {
        return successfulChecks;
    }

    public void setSuccessfulChecks(Long successfulChecks) {
        this.successfulChecks = successfulChecks;
    }

    // =========================================
    // HELPER METHODS
    // =========================================
    
    public boolean isUp() {
        return STATUS_UP.equalsIgnoreCase(status);
    }
    
    public boolean isDown() {
        return STATUS_DOWN.equalsIgnoreCase(status);
    }
    
    public boolean isChecking() {
        return status == null || STATUS_CHECKING.equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return "DashboardMonitor{" +
                "id=" + id +
                ", clientName='" + clientName + '\'' +
                ", status='" + status + '\'' +
                ", statusCode=" + statusCode +
                ", responseTime=" + responseTime +
                ", lastChecked=" + lastChecked +
                ", active=" + active +
                ", uptimePercentage=" + uptimePercentage +
                '}';
    }
}