// src/main/java/com/eduvision/service/AlertService.java
package com.eduvision.service;

import jakarta.transaction.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AlertService {

    private final JdbcTemplate jdbc;

    public AlertService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> getSessionAlerts(String sessionId) {
        return jdbc.queryForList(
                "SELECT alert_id, alert_type, severity, title, message, status, " +
                "       triggered_at, acknowledged_at, resolved_at " +
                "FROM alerts WHERE session_id = ? ORDER BY triggered_at DESC",
                sessionId);
    }

    @Transactional
    public void acknowledgeAlert(String alertId) {
        jdbc.update(
                "UPDATE alerts SET status = 'acknowledged', acknowledged_at = ? WHERE alert_id = ?",
                LocalDateTime.now(), alertId);
    }

    @Transactional
    public void resolveAlert(String alertId) {
        jdbc.update(
                "UPDATE alerts SET status = 'resolved', resolved_at = ? WHERE alert_id = ?",
                LocalDateTime.now(), alertId);
    }
}