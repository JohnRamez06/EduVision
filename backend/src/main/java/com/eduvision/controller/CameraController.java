package com.eduvision.controller;

import com.eduvision.service.CameraConfigurationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/camera")
public class CameraController {

    private final CameraConfigurationService cameraService;

    public CameraController(CameraConfigurationService cameraService) {
        this.cameraService = cameraService;
    }
    
    @PostMapping("/select")
    public ResponseEntity<Map<String, Object>> selectCamera(
            @RequestParam String cameraType,
            @RequestParam(defaultValue = "0") int deviceId,
            @RequestParam(defaultValue = "1280x720") String resolution,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String lecturerId = getLecturerIdFromEmail(userDetails.getUsername());
        
        cameraService.saveCameraConfiguration(lecturerId, cameraType, deviceId, resolution);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "saved");
        response.put("cameraType", cameraType);
        response.put("deviceId", deviceId);
        response.put("resolution", resolution);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentCamera(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String lecturerId = getLecturerIdFromEmail(userDetails.getUsername());
        Map<String, Object> config = cameraService.getCameraConfiguration(lecturerId);
        
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> listCameras() {
        List<Map<String, Object>> cameras = cameraService.getAvailableCameras();
        return ResponseEntity.ok(cameras);
    }
    
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testCamera(
            @RequestParam String cameraType,
            @RequestParam(defaultValue = "0") int deviceId) {
        
        boolean available = cameraService.testCamera(cameraType, deviceId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("cameraType", cameraType);
        response.put("deviceId", deviceId);
        response.put("available", available);
        
        return ResponseEntity.ok(response);
    }
    
    private String getLecturerIdFromEmail(String email) {
        // Implement this based on your user lookup
        // This is a placeholder - you should query your database
        return "lecturer-id-placeholder";
    }
}