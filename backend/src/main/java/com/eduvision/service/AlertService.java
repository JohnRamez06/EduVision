// src/main/java/com/eduvision/service/AlertService.java
package com.eduvision.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AlertService {

    private static final Logger logger = LoggerFactory.getLogger(AlertService.class);
    private final JdbcTemplate jdbcTemplate;

    public AlertService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // REQUIRES_NEW = runs in its own transaction so a failure here never
    // marks the caller's transaction (e.g. recordExit) as rollback-only.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Map<String, Object> createAlert(String sessionId, String title, String message, String severity, String alertType) {
        String alertId = UUID.randomUUID().toString();

        // Get course_id from session (required NOT NULL column)
        String courseId = null;
        try {
            courseId = jdbcTemplate.queryForObject(
                "SELECT course_id FROM lecture_sessions WHERE id = ?",
                String.class, sessionId
            );
        } catch (Exception e) {
            logger.warn("Could not find course for session: {}", sessionId);
        }

        if (courseId == null) {
            logger.warn("Skipping alert — no course_id found for session {}", sessionId);
            return Map.of("skipped", true, "reason", "course not found");
        }

        String sql = """
            INSERT INTO alerts (id, session_id, course_id, title, message, severity, status, triggered_at, alert_type)
            VALUES (?, ?, ?, ?, ?, ?, 'open', NOW(), ?)
        """;
        jdbcTemplate.update(sql, alertId, sessionId, courseId, title, message, severity, alertType);
        
        logger.info("✅ Alert created: {} - {}", title, alertId);
        
        // Create notifications for lecturers
        createNotificationForLecturers(alertId, sessionId, title, message);
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", alertId);
        result.put("sessionId", sessionId);
        result.put("courseId", courseId);
        result.put("title", title);
        result.put("message", message);
        result.put("severity", severity);
        result.put("alertType", alertType);
        result.put("status", "open");
        result.put("triggeredAt", LocalDateTime.now().toString());
        
        return result;
    }

    public List<Map<String, Object>> getAlertsForSession(String sessionId) {
        String sql = """
            SELECT id, session_id, course_id, title, message, severity, status, triggered_at, alert_metadata
            FROM alerts
            WHERE session_id = ?
            ORDER BY triggered_at DESC
        """;
        
        try {
            return jdbcTemplate.queryForList(sql, sessionId);
        } catch (Exception e) {
            logger.error("Error getting alerts: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> getPendingAlertsForLecturer(String email) {
        String sql = """
            SELECT a.id, a.session_id, a.course_id, a.title, a.message, a.severity, a.status, a.triggered_at, a.alert_metadata
            FROM alerts a
            JOIN lecture_sessions ls ON ls.id = a.session_id
            JOIN course_lecturers cl ON cl.course_id = ls.course_id
            JOIN users u ON u.id = cl.lecturer_id
            WHERE u.email = ? AND a.status = 'open'
            ORDER BY a.triggered_at DESC
            LIMIT 20
        """;
        
        try {
            return jdbcTemplate.queryForList(sql, email);
        } catch (Exception e) {
            logger.error("Error getting pending alerts: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Transactional
    public void markAlertAcknowledged(String alertId) {
        String sql = """
            UPDATE alerts 
            SET status = 'acknowledged', acknowledged_at = NOW()
            WHERE id = ?
        """;
        jdbcTemplate.update(sql, alertId);
        logger.info("Alert acknowledged: {}", alertId);
    }

    // ========== NOTIFICATIONS METHODS (Matching your table structure) ==========

    public List<Map<String, Object>> getNotificationsForUser(String email) {
        // Get user_id from email
        String userId = null;
        try {
            userId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE email = ?", 
                String.class, email
            );
        } catch (Exception e) {
            logger.warn("User not found: {}", email);
            return new ArrayList<>();
        }
        
        if (userId == null) {
            return new ArrayList<>();
        }
        
        // Using your actual column names: recipient_id, subject, body, channel, status, read_at
        String sql = """
            SELECT id, subject as title, body as message, channel, status, read_at, created_at
            FROM notifications
            WHERE recipient_id = ?
            ORDER BY created_at DESC
            LIMIT 50
        """;
        
        try {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, userId);
            
            // Transform to match frontend expected format
            List<Map<String, Object>> formatted = new ArrayList<>();
            for (Map<String, Object> notif : results) {
                Map<String, Object> f = new HashMap<>();
                f.put("id", notif.get("id"));
                f.put("title", notif.get("title"));
                f.put("message", notif.get("message"));
                f.put("type", notif.get("channel"));
                f.put("is_read", notif.get("read_at") != null);
                f.put("created_at", notif.get("created_at"));
                formatted.add(f);
            }
            return formatted;
        } catch (Exception e) {
            logger.warn("Could not get notifications: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Transactional
    public void markNotificationRead(String notificationId) {
        try {
            String sql = "UPDATE notifications SET status = 'read', read_at = NOW() WHERE id = ?";
            jdbcTemplate.update(sql, notificationId);
            logger.debug("Notification marked as read: {}", notificationId);
        } catch (Exception e) {
            logger.warn("Could not mark notification as read: {}", e.getMessage());
        }
    }

    @Transactional
    public void markAllNotificationsRead(String email) {
        try {
            String sql = """
                UPDATE notifications 
                SET status = 'read', read_at = NOW()
                WHERE recipient_id = (SELECT id FROM users WHERE email = ?)
                AND status != 'read'
            """;
            jdbcTemplate.update(sql, email);
            logger.info("All notifications marked as read for user: {}", email);
        } catch (Exception e) {
            logger.warn("Could not mark all notifications as read: {}", e.getMessage());
        }
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createNotificationForLecturers(String alertId, String sessionId, String title, String message) {
        try {
            // Get all lecturers for this session's course
            String sql = """
                SELECT DISTINCT cl.lecturer_id
                FROM course_lecturers cl
                JOIN lecture_sessions ls ON ls.course_id = cl.course_id
                WHERE ls.id = ?
            """;
            
            List<String> lecturerIds = jdbcTemplate.queryForList(sql, String.class, sessionId);
            
            if (lecturerIds.isEmpty()) {
                logger.debug("No lecturers found for session: {}", sessionId);
                return;
            }
            
            // Using your actual notification table columns
            String insertSql = """
                INSERT INTO notifications (id, alert_id, recipient_id, channel, status, subject, body, created_at)
                VALUES (UUID(), ?, ?, 'in_app', 'pending', ?, ?, NOW())
            """;
            
            for (String lecturerId : lecturerIds) {
                jdbcTemplate.update(insertSql, alertId, lecturerId, title, message);
            }
            
            logger.info("Created {} notifications for alert {}", lecturerIds.size(), alertId);
        } catch (Exception e) {
            logger.warn("Could not create notifications: {}", e.getMessage());
        }
    }
}