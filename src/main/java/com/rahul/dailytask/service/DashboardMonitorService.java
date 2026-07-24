package com.rahul.dailytask.service;

import com.rahul.dailytask.entity.DashboardMonitor;
import com.rahul.dailytask.repository.DashboardMonitorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DashboardMonitorService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardMonitorService.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private DashboardMonitorRepository dashboardMonitorRepository;

    @Value("${app.ssl.verification.enabled:false}")
    private boolean sslVerificationEnabled;

    // =========================================
    // DISABLE SSL VERIFICATION FOR TESTING
    // =========================================
    @PostConstruct
    public void initSslVerification() {
        if (!sslVerificationEnabled) {
            logger.warn("SSL Verification is DISABLED - This should only be used in development!");
            disableSslVerification();
        } else {
            logger.info("SSL Verification is ENABLED");
        }
    }

    private void disableSslVerification() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                }
            };
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        } catch (Exception e) {
            logger.error("Failed to disable SSL verification", e);
        }
    }

    // =========================================
    // GET ALL DASHBOARDS
    // =========================================
    public List<DashboardMonitor> getAllDashboards() {
        return dashboardMonitorRepository.findAllByOrderByLastCheckedDesc();
    }

    // =========================================
    // GET ACTIVE DASHBOARDS
    // =========================================
    public List<DashboardMonitor> getActiveDashboards() {
        return dashboardMonitorRepository.findByActiveTrue();
    }

    // =========================================
    // GET DASHBOARD BY ID
    // =========================================
    public Optional<DashboardMonitor> getDashboardById(Long id) {
        return dashboardMonitorRepository.findById(id);
    }

    // =========================================
    // SAVE DASHBOARD WITH VALIDATION
    // =========================================
    public DashboardMonitor saveDashboard(DashboardMonitor dashboard) {
        if (!isValidUrl(dashboard.getDashboardUrl())) {
            throw new RuntimeException("Invalid URL format: " + dashboard.getDashboardUrl());
        }

        initializeDefaultValues(dashboard);
        
        logger.info("Saving dashboard: {} ({})", dashboard.getClientName(), dashboard.getDashboardUrl());
        return dashboardMonitorRepository.save(dashboard);
    }

    // =========================================
    // UPDATE DASHBOARD STATUS
    // =========================================
    public DashboardMonitor updateDashboardStatus(Long id, String status, Integer statusCode,
                                                   Long responseTime, String remarks) {
        DashboardMonitor dashboard = findDashboardOrThrow(id);
        initializeDefaultValues(dashboard);

        dashboard.setStatus(status);
        dashboard.setStatusCode(statusCode);
        dashboard.setResponseTime(responseTime);
        dashboard.setLastChecked(LocalDateTime.now());
        dashboard.setRemarks(remarks);

        updateStatistics(dashboard, status);

        logger.debug("Updated dashboard {} status: {} (HTTP {})", id, status, statusCode);
        return dashboardMonitorRepository.save(dashboard);
    }

    // =========================================
    // DELETE DASHBOARD
    // =========================================
    public void deleteDashboard(Long id) {
        if (!dashboardMonitorRepository.existsById(id)) {
            throw new RuntimeException("Dashboard not found with id: " + id);
        }
        dashboardMonitorRepository.deleteById(id);
        logger.info("Deleted dashboard with id: {}", id);
    }

    // =========================================
    // TOGGLE ACTIVE STATUS
    // =========================================
    public DashboardMonitor toggleActive(Long id) {
        DashboardMonitor dashboard = findDashboardOrThrow(id);
        dashboard.setActive(!dashboard.getActive());
        logger.info("Toggled dashboard {} active status to: {}", id, dashboard.getActive());
        return dashboardMonitorRepository.save(dashboard);
    }

    // =========================================
    // GET STATISTICS
    // =========================================
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long total = dashboardMonitorRepository.countActiveDashboards();
        long up = dashboardMonitorRepository.countUpDashboards();
        long down = dashboardMonitorRepository.countDownDashboards();
        long checking = dashboardMonitorRepository.countCheckingDashboards();
        
        stats.put("total", total);
        stats.put("up", up);
        stats.put("down", down);
        stats.put("checking", checking);
        
        Double avgResponse = dashboardMonitorRepository.averageResponseTime();
        stats.put("averageResponse", avgResponse != null ? Math.round(avgResponse) : 0);
        
        stats.put("lastUpdated", LocalDateTime.now().format(formatter));
        
        return stats;
    }

    // =========================================
    // GET DASHBOARDS BY STATUS
    // =========================================
    public List<DashboardMonitor> getDashboardsByStatus(String status) {
        return dashboardMonitorRepository.findActiveByStatus(status);
    }

    // =========================================
    // SEARCH DASHBOARDS
    // =========================================
    public List<DashboardMonitor> searchDashboards(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllDashboards();
        }
        return dashboardMonitorRepository.searchActive(keyword.trim());
    }

    // =========================================
    // CHECK A SINGLE DASHBOARD
    // =========================================
    public DashboardMonitor checkDashboard(Long id) {
        DashboardMonitor dashboard = findDashboardOrThrow(id);
        return checkDashboardUrl(dashboard);
    }

    // =========================================
    // CHECK ALL DASHBOARDS
    // =========================================
    public List<DashboardMonitor> checkAllDashboards() {
        logger.info("Starting check for all active dashboards");
        List<DashboardMonitor> dashboards = dashboardMonitorRepository.findByActiveTrue();
        
        if (dashboards.isEmpty()) {
            logger.warn("No active dashboards to check");
            return Collections.emptyList();
        }

        logger.info("Checking {} active dashboards", dashboards.size());
        
        for (DashboardMonitor dashboard : dashboards) {
            checkDashboardUrl(dashboard);
        }
        
        logger.info("Completed check for {} dashboards", dashboards.size());
        return dashboards;
    }

    // =========================================
    // URL CHECKING METHOD
    // =========================================
    private DashboardMonitor checkDashboardUrl(DashboardMonitor dashboard) {
        try {
            initializeDefaultValues(dashboard);

            String urlString = normalizeUrl(dashboard.getDashboardUrl());
            logger.info("Checking: {} - {}", dashboard.getClientName(), urlString);

            long startTime = System.currentTimeMillis();
            
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.setInstanceFollowRedirects(true);
            
            connection.setRequestProperty("User-Agent", 
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            connection.setRequestProperty("Accept", 
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
            connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
            connection.setRequestProperty("Connection", "keep-alive");
            connection.setRequestProperty("Cache-Control", "max-age=0");

            int responseCode = connection.getResponseCode();
            long responseTime = System.currentTimeMillis() - startTime;
            
            connection.disconnect();

            // Determine status
            String status;
            String remarks;
            
            if (responseCode >= 200 && responseCode < 300) {
                status = "UP";
                remarks = "Dashboard reachable (HTTP " + responseCode + ")";
            } else if (responseCode >= 300 && responseCode < 400) {
                status = "UP";
                remarks = "Redirect (HTTP " + responseCode + ") - Accessible";
            } else if (responseCode == 401 || responseCode == 403) {
                status = "UP";
                remarks = "Authentication required (HTTP " + responseCode + ") - Accessible";
            } else if (responseCode == 404) {
                status = "UP";
                remarks = "Page not found (HTTP 404) - Server accessible";
            } else if (responseCode >= 500) {
                status = "DOWN";
                remarks = "Server Error (HTTP " + responseCode + ")";
            } else {
                status = "UP";
                remarks = "HTTP " + responseCode + " - Server is accessible";
            }
            
            updateDashboardAfterCheck(dashboard, status, responseCode, responseTime, remarks);
            
            if (status.equals("UP")) {
                logger.info("SUCCESS: {} - UP ({}ms) - HTTP {}", dashboard.getClientName(), responseTime, responseCode);
            } else {
                logger.info("FAILED: {} - DOWN ({}ms) - HTTP {}", dashboard.getClientName(), responseTime, responseCode);
            }
            
            return dashboardMonitorRepository.save(dashboard);
            
        } catch (SocketTimeoutException e) {
            logger.warn("Timeout for {}, trying GET method...", dashboard.getClientName());
            return checkWithGetMethod(dashboard, normalizeUrl(dashboard.getDashboardUrl()));
            
        } catch (UnknownHostException e) {
            logger.error("DNS Error for {}: {}", dashboard.getClientName(), e.getMessage());
            markAsDown(dashboard, "DNS Resolution Failed: " + e.getMessage());
            return dashboardMonitorRepository.save(dashboard);
            
        } catch (IOException e) {
            logger.warn("Connection error for {}, trying GET method...", dashboard.getClientName());
            return checkWithGetMethod(dashboard, normalizeUrl(dashboard.getDashboardUrl()));
            
        } catch (Exception e) {
            logger.error("Unexpected error for {}: {}", dashboard.getClientName(), e.getMessage());
            markAsDown(dashboard, "Unexpected Error: " + e.getMessage());
            return dashboardMonitorRepository.save(dashboard);
        }
    }

    // =========================================
    // FALLBACK: Check with GET method
    // =========================================
    private DashboardMonitor checkWithGetMethod(DashboardMonitor dashboard, String urlString) {
        try {
            logger.info("Retrying {} with GET method...", dashboard.getClientName());
            
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(20000);
            connection.setReadTimeout(20000);
            connection.setInstanceFollowRedirects(true);
            
            connection.setRequestProperty("User-Agent", 
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            connection.setRequestProperty("Accept", 
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
            connection.setRequestProperty("Connection", "keep-alive");
            
            long startTime = System.currentTimeMillis();
            int responseCode = connection.getResponseCode();
            long responseTime = System.currentTimeMillis() - startTime;
            
            connection.disconnect();

            if (responseCode > 0) {
                String status;
                String remarks;
                
                if (responseCode >= 500) {
                    status = "DOWN";
                    remarks = "Server Error (HTTP " + responseCode + ")";
                } else {
                    status = "UP";
                    if (responseCode >= 300 && responseCode < 400) {
                        remarks = "Redirect (HTTP " + responseCode + ") - Accessible";
                    } else if (responseCode == 401 || responseCode == 403) {
                        remarks = "Authentication required (HTTP " + responseCode + ") - Accessible";
                    } else if (responseCode == 404) {
                        remarks = "Page not found (HTTP 404) - Server accessible";
                    } else {
                        remarks = "Dashboard reachable (HTTP " + responseCode + ")";
                    }
                }
                
                updateDashboardAfterCheck(dashboard, status, responseCode, responseTime, remarks);
                logger.info("GET fallback: {} - {} ({}ms) - HTTP {}", 
                    dashboard.getClientName(), status, responseTime, responseCode);
                return dashboardMonitorRepository.save(dashboard);
            }
            
        } catch (Exception e) {
            logger.error("GET fallback failed for {}: {}", dashboard.getClientName(), e.getMessage());
        }
        
        markAsDown(dashboard, "All connection attempts failed");
        return dashboardMonitorRepository.save(dashboard);
    }

    // =========================================
    // HELPER METHODS
    // =========================================

    private void updateDashboardAfterCheck(DashboardMonitor dashboard, String status, 
                                           int statusCode, long responseTime, String remarks) {
        dashboard.setStatus(status);
        dashboard.setStatusCode(statusCode);
        dashboard.setResponseTime(responseTime);
        dashboard.setLastChecked(LocalDateTime.now());
        dashboard.setRemarks(remarks);
        updateStatistics(dashboard, status);
    }

    private void markAsDown(DashboardMonitor dashboard, String errorMessage) {
        dashboard.setStatus("DOWN");
        dashboard.setStatusCode(null);
        dashboard.setResponseTime(0L);
        dashboard.setLastChecked(LocalDateTime.now());
        dashboard.setRemarks("Failed: " + errorMessage);
        updateStatistics(dashboard, "DOWN");
    }

    private void updateStatistics(DashboardMonitor dashboard, String status) {
        long totalChecks = dashboard.getTotalChecks() != null ? dashboard.getTotalChecks() : 0;
        dashboard.setTotalChecks(totalChecks + 1);
        
        if ("UP".equalsIgnoreCase(status)) {
            long successfulChecks = dashboard.getSuccessfulChecks() != null ? dashboard.getSuccessfulChecks() : 0;
            dashboard.setSuccessfulChecks(successfulChecks + 1);
        }
        
        if (dashboard.getTotalChecks() > 0) {
            double uptime = (double) dashboard.getSuccessfulChecks() / dashboard.getTotalChecks() * 100;
            dashboard.setUptimePercentage(Math.round(uptime * 100.0) / 100.0);
        }
    }

    private void initializeDefaultValues(DashboardMonitor dashboard) {
        if (dashboard.getCreatedDate() == null) {
            dashboard.setCreatedDate(LocalDateTime.now());
        }
        if (dashboard.getActive() == null) {
            dashboard.setActive(true);
        }
        if (dashboard.getTotalChecks() == null) {
            dashboard.setTotalChecks(0L);
        }
        if (dashboard.getSuccessfulChecks() == null) {
            dashboard.setSuccessfulChecks(0L);
        }
        if (dashboard.getUptimePercentage() == null) {
            dashboard.setUptimePercentage(100.0);
        }
        if (dashboard.getStatus() == null) {
            dashboard.setStatus(DashboardMonitor.STATUS_CHECKING);
        }
        if (dashboard.getStatusCode() == null) {
            dashboard.setStatusCode(0);
        }
        if (dashboard.getResponseTime() == null) {
            dashboard.setResponseTime(0L);
        }
        if (dashboard.getRemarks() == null) {
            dashboard.setRemarks("");
        }
    }

    private String normalizeUrl(String url) {
        if (url == null) {
            throw new RuntimeException("URL cannot be null");
        }
        String trimmedUrl = url.trim();
        if (trimmedUrl.isEmpty()) {
            throw new RuntimeException("URL cannot be empty");
        }
        
        if (trimmedUrl.contains(",")) {
            String[] urls = trimmedUrl.split(",");
            trimmedUrl = urls[0].trim();
        }
        
        trimmedUrl = trimmedUrl.replaceAll("\\s+", "");
        
        if (!trimmedUrl.startsWith("http://") && !trimmedUrl.startsWith("https://")) {
            trimmedUrl = "https://" + trimmedUrl;
        }
        
        return trimmedUrl;
    }

    private boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        try {
            String normalizedUrl = normalizeUrl(url);
            new URL(normalizedUrl);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    private DashboardMonitor findDashboardOrThrow(Long id) {
        return dashboardMonitorRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Dashboard not found with id: " + id));
    }
}