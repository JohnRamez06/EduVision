// src/main/java/com/eduvision/controller/AlertController.java
package com.eduvision.controller;

import com.eduvision.service.AlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {

    private static final Logger logger = LoggerFactory.getLogger(AlertController.class);
    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @PostMapping
    public ResponseEntity<?> createAlert(@RequestBody Map<String, Object> request) {
        try {
            String sessionId = (String) request.get("sessionId");
            String title = (String) request.get("title");
            String message = (String) request.get("message");
            String severity = (String) request.get("severity");
            String alertType = (String) request.getOrDefault("alertType", "general");
            
            Map<String, Object> alert = alertService.createAlert(sessionId, title, message, severity, alertType);
            return ResponseEntity.ok(alert);
        } catch (Exception e) {
            logger.error("Error creating alert: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Map<String, Object>>> getPendingAlerts(
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        return ResponseEntity.ok(alertService.getPendingAlertsForLecturer(email));
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<Map<String, Object>>> getSessionAlerts(@PathVariable String sessionId) {
        return ResponseEntity.ok(alertService.getAlertsForSession(sessionId));
    }

    @PostMapping("/{alertId}/acknowledge")
    public ResponseEntity<?> acknowledgeAlert(@PathVariable String alertId) {
        alertService.markAlertAcknowledged(alertId);
        return ResponseEntity.ok(Map.of("message", "Alert acknowledged"));
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<Map<String, Object>>> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        return ResponseEntity.ok(alertService.getNotificationsForUser(email));
    }

    @PostMapping("/notifications/{notificationId}/read")
    public ResponseEntity<?> markNotificationRead(@PathVariable String notificationId) {
        alertService.markNotificationRead(notificationId);
        return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
    }

    @PostMapping("/notifications/read-all")
    public ResponseEntity<?> markAllNotificationsRead(
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        alertService.markAllNotificationsRead(email);
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
    }

    // Test endpoint to manually trigger an alert
@PostMapping("/test")
public ResponseEntity<?> testAlert() {
    Map<String, Object> testAlert = Map.of(
        "sessionId", "test-session",
        "title", "🧪 Test Alert",
        "message", "This is a test alert to verify the system is working",
        "severity", "info",
        "alertType", "test"
    );
    
    try {
        Map<String, Object> result = alertService.createAlert(
            "test-session", 
            "🧪 Test Alert", 
            "This is a test alert to verify the system is working", 
            "info", 
            "test"
        );
        return ResponseEntity.ok(result);
    } catch (Exception e) {
        return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
    }
}
}