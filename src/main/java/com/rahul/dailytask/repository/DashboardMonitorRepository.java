package com.rahul.dailytask.repository;

import com.rahul.dailytask.entity.DashboardMonitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DashboardMonitorRepository extends JpaRepository<DashboardMonitor, Long> {

    // Find all active dashboards
    List<DashboardMonitor> findByActiveTrue();

    // Find by status (case insensitive)
    List<DashboardMonitor> findByStatusIgnoreCase(String status);

    // Find by client name (contains, case insensitive)
    List<DashboardMonitor> findByClientNameContainingIgnoreCase(String clientName);

    // Count by status (only active dashboards)
    @Query("SELECT COUNT(d) FROM DashboardMonitor d WHERE d.active = true AND UPPER(d.status) = UPPER(:status)")
    long countByStatus(@Param("status") String status);

    // Count active dashboards
    @Query("SELECT COUNT(d) FROM DashboardMonitor d WHERE d.active = true")
    long countActiveDashboards();

    // Count UP dashboards (only active)
    @Query("SELECT COUNT(d) FROM DashboardMonitor d WHERE d.active = true AND UPPER(d.status) = 'UP'")
    long countUpDashboards();

    // Count DOWN dashboards (only active)
    @Query("SELECT COUNT(d) FROM DashboardMonitor d WHERE d.active = true AND UPPER(d.status) = 'DOWN'")
    long countDownDashboards();

    // Count CHECKING dashboards (only active)
    @Query("SELECT COUNT(d) FROM DashboardMonitor d WHERE d.active = true AND (d.status IS NULL OR UPPER(d.status) = 'CHECKING')")
    long countCheckingDashboards();

    // Average response time for UP dashboards (only active, ignore nulls)
    @Query("SELECT AVG(d.responseTime) FROM DashboardMonitor d WHERE d.active = true AND UPPER(d.status) = 'UP' AND d.responseTime IS NOT NULL")
    Double averageResponseTime();

    // Find by URL
    Optional<DashboardMonitor> findByDashboardUrl(String dashboardUrl);

    // Get all dashboards sorted by last checked (newest first)
    List<DashboardMonitor> findAllByOrderByLastCheckedDesc();

    // Get all active dashboards sorted by last checked
    @Query("SELECT d FROM DashboardMonitor d WHERE d.active = true ORDER BY d.lastChecked DESC")
    List<DashboardMonitor> findAllActiveOrderByLastCheckedDesc();

    // Search by client name or URL (case insensitive)
    @Query("SELECT d FROM DashboardMonitor d WHERE d.active = true AND " +
           "(LOWER(d.clientName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.dashboardUrl) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<DashboardMonitor> searchActive(@Param("keyword") String keyword);

    // Find dashboards with status
    @Query("SELECT d FROM DashboardMonitor d WHERE d.active = true AND UPPER(d.status) = UPPER(:status)")
    List<DashboardMonitor> findActiveByStatus(@Param("status") String status);
}