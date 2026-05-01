// src/main/java/com/eduvision/controller/AlertController.java
package com.eduvision.controller;

import com.eduvision.service.AlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<Map<String, Object>>> getSessionAlerts(
            @PathVariable String sessionId) {
        return ResponseEntity.ok(alertService.getSessionAlerts(sessionId));
    }

    @PutMapping("/{alertId}/acknowledge")
    public ResponseEntity<Void> acknowledgeAlert(@PathVariable String alertId) {
        alertService.acknowledgeAlert(alertId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{alertId}/resolve")
    public ResponseEntity<Void> resolveAlert(@PathVariable String alertId) {
        alertService.resolveAlert(alertId);
        return ResponseEntity.ok().build();
    }
}