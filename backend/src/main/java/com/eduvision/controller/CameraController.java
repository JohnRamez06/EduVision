package com.eduvision.controller;

import com.eduvision.dto.camera.CameraConfigDTO;
import com.eduvision.dto.camera.CameraTestRequest;
import com.eduvision.service.CameraConfigurationService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/camera")
public class CameraController {

    private final CameraConfigurationService cameraConfigurationService;

    public CameraController(CameraConfigurationService cameraConfigurationService) {
        this.cameraConfigurationService = cameraConfigurationService;
    }

    @GetMapping("/configs")
    public ResponseEntity<List<CameraConfigDTO>> getConfigs(@RequestParam(value = "userId", required = false) String userId) {
        return ResponseEntity.ok(cameraConfigurationService.getConfigs(userId));
    }

    @PostMapping("/configs")
    public ResponseEntity<CameraConfigDTO> createConfig(@RequestBody CameraConfigDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cameraConfigurationService.createConfig(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CameraConfigDTO> updateConfig(@PathVariable String id, @RequestBody CameraConfigDTO request) {
        return ResponseEntity.ok(cameraConfigurationService.updateConfig(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConfig(@PathVariable String id) {
        cameraConfigurationService.deleteConfig(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/test")
    public ResponseEntity<Map<String, Object>> testConnection(@PathVariable String id, @RequestBody(required = false) CameraTestRequest request) {
        return ResponseEntity.ok(cameraConfigurationService.testConnection(id, request));
    }
}
