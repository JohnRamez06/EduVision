package com.eduvision.controller;

import com.eduvision.dto.alert.AlertDTO;
import com.eduvision.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {

    @Autowired
    private AlertService alertService;

    @GetMapping("/session/{id}")
    public ResponseEntity<List<AlertDTO>> getAlertsBySession(@PathVariable String id) {
        List<AlertDTO> alerts = alertService.getAlertsBySession(id);
        return ResponseEntity.ok(alerts);
    }

    @PutMapping("/{id}/acknowledge")
    public ResponseEntity<Void> acknowledgeAlert(@PathVariable String id, @RequestParam String userId) {
        boolean success = alertService.acknowledgeAlert(id, userId);
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<Void> resolveAlert(@PathVariable String id, @RequestParam String userId) {
        boolean success = alertService.resolveAlert(id, userId);
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
