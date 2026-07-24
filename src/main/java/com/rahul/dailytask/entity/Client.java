package com.rahul.dailytask.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_name", nullable = false)
    private String clientName;

    @Column(name = "dashboard_url")
    private String dashboardUrl;

    @Column(name = "uat_whatsapp")
    private String uatWhatsapp;

    @Column(name = "prod_whatsapp")
    private String prodWhatsapp;

    @Column(name = "client_number")
    private String clientNumber;

    @Column(name = "status")
    private String status;

    @Column(name = "uat_dashboard_url")
    private String uatDashboardUrl;

    @Column(name = "prod_lab_url")
    private String prodLabUrl;

    @Column(name = "uat_lab_url")
    private String uatLabUrl;

    @Column(name = "last_updated")
    private String lastUpdated;

    // =========================================
    // CONSTRUCTORS
    // =========================================

    public Client() {
        this.status = "200";
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

    public String getUatWhatsapp() {
        return uatWhatsapp;
    }

    public void setUatWhatsapp(String uatWhatsapp) {
        this.uatWhatsapp = uatWhatsapp;
    }

    public String getProdWhatsapp() {
        return prodWhatsapp;
    }

    public void setProdWhatsapp(String prodWhatsapp) {
        this.prodWhatsapp = prodWhatsapp;
    }

    public String getClientNumber() {
        return clientNumber;
    }

    public void setClientNumber(String clientNumber) {
        this.clientNumber = clientNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUatDashboardUrl() {
        return uatDashboardUrl;
    }

    public void setUatDashboardUrl(String uatDashboardUrl) {
        this.uatDashboardUrl = uatDashboardUrl;
    }

    public String getProdLabUrl() {
        return prodLabUrl;
    }

    public void setProdLabUrl(String prodLabUrl) {
        this.prodLabUrl = prodLabUrl;
    }

    public String getUatLabUrl() {
        return uatLabUrl;
    }

    public void setUatLabUrl(String uatLabUrl) {
        this.uatLabUrl = uatLabUrl;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", clientName='" + clientName + '\'' +
                ", dashboardUrl='" + dashboardUrl + '\'' +
                ", status='" + status + '\'' +
                ", lastUpdated='" + lastUpdated + '\'' +
                '}';
    }
}