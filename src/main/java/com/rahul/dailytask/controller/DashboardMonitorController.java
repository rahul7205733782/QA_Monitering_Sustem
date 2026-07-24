package com.rahul.dailytask.controller;

import com.rahul.dailytask.entity.DashboardMonitor;
import com.rahul.dailytask.service.DashboardMonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/dashboard-monitor")
@CrossOrigin(origins = "*")
public class DashboardMonitorController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardMonitorController.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private DashboardMonitorService dashboardMonitorService;

    // =========================================
    // GET ALL DASHBOARDS
    // =========================================
    @GetMapping
    public ResponseEntity<List<DashboardMonitor>> getAllDashboards() {
        logger.debug("Fetching all dashboards");
        List<DashboardMonitor> dashboards = dashboardMonitorService.getAllDashboards();
        return ResponseEntity.ok(dashboards);
    }

    // =========================================
    // GET ACTIVE DASHBOARDS
    // =========================================
    @GetMapping("/active")
    public ResponseEntity<List<DashboardMonitor>> getActiveDashboards() {
        logger.debug("Fetching active dashboards");
        List<DashboardMonitor> dashboards = dashboardMonitorService.getActiveDashboards();
        return ResponseEntity.ok(dashboards);
    }

    // =========================================
    // GET DASHBOARD BY ID
    // =========================================
    @GetMapping("/{id}")
    public ResponseEntity<DashboardMonitor> getDashboardById(@PathVariable Long id) {
        logger.debug("Fetching dashboard with id: {}", id);
        Optional<DashboardMonitor> dashboard = dashboardMonitorService.getDashboardById(id);
        return dashboard.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================================
    // CREATE DASHBOARD
    // =========================================
    @PostMapping
    public ResponseEntity<?> createDashboard(@RequestBody DashboardMonitor dashboard) {
        try {
            logger.info("Creating new dashboard: {}", dashboard.getClientName());
            
            // Trim and validate
            if (dashboard.getClientName() != null) {
                dashboard.setClientName(dashboard.getClientName().trim());
            }
            if (dashboard.getDashboardUrl() != null) {
                dashboard.setDashboardUrl(dashboard.getDashboardUrl().trim());
            }
            
            // Validate required fields
            if (dashboard.getClientName() == null || dashboard.getClientName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Client name is required",
                    "timestamp", LocalDateTime.now().format(formatter)
                ));
            }
            
            if (dashboard.getDashboardUrl() == null || dashboard.getDashboardUrl().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Dashboard URL is required",
                    "timestamp", LocalDateTime.now().format(formatter)
                ));
            }
            
            DashboardMonitor savedDashboard = dashboardMonitorService.saveDashboard(dashboard);
            logger.info("Created dashboard with id: {}", savedDashboard.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDashboard);
            
        } catch (RuntimeException e) {
            logger.error("Error creating dashboard: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now().format(formatter)
            ));
        }
    }

    // =========================================
    // UPDATE DASHBOARD
    // =========================================
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDashboard(@PathVariable Long id, 
                                            @RequestBody DashboardMonitor dashboard) {
        try {
            logger.info("Updating dashboard with id: {}", id);
            
            // Check if dashboard exists
            Optional<DashboardMonitor> existingDashboard = dashboardMonitorService.getDashboardById(id);
            if (existingDashboard.isEmpty()) {
                logger.warn("Dashboard not found with id: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            // Trim and validate
            if (dashboard.getClientName() != null) {
                dashboard.setClientName(dashboard.getClientName().trim());
            }
            if (dashboard.getDashboardUrl() != null) {
                dashboard.setDashboardUrl(dashboard.getDashboardUrl().trim());
            }
            
            // Set the ID to ensure we're updating the existing record
            dashboard.setId(id);
            
            // Preserve existing values if not provided
            DashboardMonitor existing = existingDashboard.get();
            if (dashboard.getCreatedDate() == null) {
                dashboard.setCreatedDate(existing.getCreatedDate());
            }
            if (dashboard.getActive() == null) {
                dashboard.setActive(existing.getActive());
            }
            if (dashboard.getTotalChecks() == null) {
                dashboard.setTotalChecks(existing.getTotalChecks());
            }
            if (dashboard.getSuccessfulChecks() == null) {
                dashboard.setSuccessfulChecks(existing.getSuccessfulChecks());
            }
            if (dashboard.getUptimePercentage() == null) {
                dashboard.setUptimePercentage(existing.getUptimePercentage());
            }
            
            DashboardMonitor updatedDashboard = dashboardMonitorService.saveDashboard(dashboard);
            logger.info("Updated dashboard with id: {}", id);
            return ResponseEntity.ok(updatedDashboard);
            
        } catch (RuntimeException e) {
            logger.error("Error updating dashboard {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now().format(formatter)
            ));
        }
    }

    // =========================================
    // UPDATE DASHBOARD STATUS (PATCH)
    // =========================================
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                         @RequestParam String status,
                                         @RequestParam(required = false) Integer statusCode,
                                         @RequestParam(required = false) Long responseTime,
                                         @RequestParam(required = false) String remarks) {
        try {
            logger.info("Updating status for dashboard {} to {}", id, status);
            DashboardMonitor updated = dashboardMonitorService.updateDashboardStatus(
                id, status, statusCode, responseTime, remarks
            );
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            logger.warn("Dashboard not found with id: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    // =========================================
    // DELETE DASHBOARD
    // =========================================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDashboard(@PathVariable Long id) {
        try {
            logger.info("Deleting dashboard with id: {}", id);
            dashboardMonitorService.deleteDashboard(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.warn("Dashboard not found with id: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    // =========================================
    // TOGGLE ACTIVE STATUS
    // =========================================
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<?> toggleActive(@PathVariable Long id) {
        try {
            logger.info("Toggling active status for dashboard {}", id);
            DashboardMonitor updated = dashboardMonitorService.toggleActive(id);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            logger.warn("Dashboard not found with id: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    // =========================================
    // GET STATISTICS - IMPROVED
    // =========================================
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> stats = dashboardMonitorService.getStatistics();
            
            // Add additional validation
            long total = (long) stats.get("total");
            long up = (long) stats.get("up");
            long down = (long) stats.get("down");
            long checking = (long) stats.get("checking");
            
            // Ensure consistency
            if (up + down + checking != total) {
                logger.warn("⚠️ Returning corrected statistics - original sums didn't match total");
                // Force correction
                stats.put("up", Math.min(up, total));
                stats.put("down", Math.min(down, total));
                stats.put("checking", total - Math.min(up, total) - Math.min(down, total));
            }
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error fetching statistics: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch statistics"));
        }
    }

    // =========================================
    // GET DASHBOARDS BY STATUS
    // =========================================
    @GetMapping("/status/{status}")
    public ResponseEntity<List<DashboardMonitor>> getDashboardsByStatus(@PathVariable String status) {
        logger.debug("Fetching dashboards with status: {}", status);
        List<DashboardMonitor> dashboards = dashboardMonitorService.getDashboardsByStatus(status);
        return ResponseEntity.ok(dashboards);
    }

    // =========================================
    // SEARCH DASHBOARDS
    // =========================================
    @GetMapping("/search")
    public ResponseEntity<List<DashboardMonitor>> searchDashboards(@RequestParam String keyword) {
        logger.debug("Searching dashboards with keyword: {}", keyword);
        List<DashboardMonitor> dashboards = dashboardMonitorService.searchDashboards(keyword);
        return ResponseEntity.ok(dashboards);
    }

    // =========================================
    // CHECK A SINGLE DASHBOARD
    // =========================================
    @PostMapping("/{id}/check")
    public ResponseEntity<?> checkDashboard(@PathVariable Long id) {
        try {
            logger.info("Checking dashboard with id: {}", id);
            DashboardMonitor checked = dashboardMonitorService.checkDashboard(id);
            return ResponseEntity.ok(checked);
        } catch (RuntimeException e) {
            logger.warn("Dashboard not found with id: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    // =========================================
    // CHECK ALL DASHBOARDS
    // =========================================
    @PostMapping("/check-all")
    public ResponseEntity<List<DashboardMonitor>> checkAllDashboards() {
        logger.info("Checking all active dashboards");
        List<DashboardMonitor> updated = dashboardMonitorService.checkAllDashboards();
        logger.info("Checked {} dashboards", updated.size());
        return ResponseEntity.ok(updated);
    }

    // =========================================
    // HEALTH CHECK ENDPOINT
    // =========================================
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        logger.debug("Health check requested");
        Map<String, String> health = Map.of(
            "status", "UP",
            "service", "Dashboard Monitor Service",
            "timestamp", LocalDateTime.now().format(formatter)
        );
        return ResponseEntity.ok(health);
    }

    // =========================================
    // BULK DELETE
    // =========================================
    @DeleteMapping("/bulk")
    public ResponseEntity<?> deleteMultipleDashboards(@RequestBody List<Long> ids) {
        try {
            logger.info("Deleting {} dashboards", ids.size());
            for (Long id : ids) {
                dashboardMonitorService.deleteDashboard(id);
            }
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.error("Error during bulk delete: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now().format(formatter)
            ));
        }
    }
}