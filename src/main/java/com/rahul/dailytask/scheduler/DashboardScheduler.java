package com.rahul.dailytask.scheduler;

import com.rahul.dailytask.entity.DashboardMonitor;
import com.rahul.dailytask.service.DashboardMonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class DashboardScheduler {

    private static final Logger logger = LoggerFactory.getLogger(DashboardScheduler.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private DashboardMonitorService dashboardMonitorService;

    @Value("${monitor.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    @Scheduled(fixedDelayString = "${monitor.scheduler.fixed-delay:300000}")
    public void monitorDashboards() {
        if (!schedulerEnabled) {
            logger.debug("Scheduler is disabled. Skipping dashboard check.");
            return;
        }

        logger.info("==========================================");
        logger.info("Starting scheduled dashboard monitoring at {}", 
            LocalDateTime.now().format(formatter));
        logger.info("==========================================");

        try {
            long startTime = System.currentTimeMillis();
            
            List<DashboardMonitor> activeDashboards = dashboardMonitorService.getActiveDashboards();
            
            if (activeDashboards.isEmpty()) {
                logger.info("No active dashboards to monitor. Skipping check.");
                return;
            }
            
            logger.info("Found {} active dashboards to check", activeDashboards.size());
            
            List<DashboardMonitor> results = dashboardMonitorService.checkAllDashboards();
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            long upCount = results.stream()
                .filter(d -> d.getStatus() != null && d.getStatus().equalsIgnoreCase("UP"))
                .count();
            long downCount = results.stream()
                .filter(d -> d.getStatus() != null && d.getStatus().equalsIgnoreCase("DOWN"))
                .count();

            logger.info("==========================================");
            logger.info("Scheduled monitoring completed in {} ms", duration);
            logger.info("Summary:");
            logger.info("   Total Checked: {}", results.size());
            logger.info("   UP: {}", upCount);
            logger.info("   DOWN: {}", downCount);
            logger.info("==========================================");

        } catch (Exception e) {
            logger.error("Error in scheduled monitoring: {}", e.getMessage(), e);
        }
    }
}