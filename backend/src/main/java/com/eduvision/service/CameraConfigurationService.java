package com.eduvision.service;

import com.eduvision.factory.CameraConfigurationFactory;
import com.eduvision.factory.VideoStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CameraConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(CameraConfigurationService.class);
    private final JdbcTemplate jdbcTemplate;
    private final CameraConfigurationFactory cameraFactory;

    // In-memory cache for active camera sessions
    private final Map<String, CameraSession> activeCameras = new HashMap<>();

    public CameraConfigurationService(JdbcTemplate jdbcTemplate, CameraConfigurationFactory cameraFactory) {
        this.jdbcTemplate = jdbcTemplate;
        this.cameraFactory = cameraFactory;
    }

    /**
     * Save camera configuration for a lecturer
     */
    @Transactional
    public void saveCameraConfiguration(String lecturerId, String cameraType, int deviceId, String resolution) {
        String sql = """
            INSERT INTO camera_configurations (id, lecturer_id, camera_type, device_id, resolution, is_active, created_at)
            VALUES (UUID(), ?, ?, ?, ?, TRUE, NOW())
            ON DUPLICATE KEY UPDATE
            camera_type = VALUES(camera_type),
            device_id = VALUES(device_id),
            resolution = VALUES(resolution),
            is_active = TRUE,
            updated_at = NOW()
        """;
        
        jdbcTemplate.update(sql, lecturerId, cameraType, deviceId, resolution);
        logger.info("✅ Camera configuration saved for lecturer {}: type={}, device={}", lecturerId, cameraType, deviceId);
    }

    /**
     * Get camera configuration for a lecturer
     */
    public Map<String, Object> getCameraConfiguration(String lecturerId) {
        String sql = """
            SELECT camera_type, device_id, resolution, is_active
            FROM camera_configurations
            WHERE lecturer_id = ? AND is_active = TRUE
            ORDER BY created_at DESC
            LIMIT 1
        """;
        
        try {
            Map<String, Object> config = jdbcTemplate.queryForMap(sql, lecturerId);
            return config;
        } catch (Exception e) {
            // Return default configuration if none exists
            Map<String, Object> defaultConfig = new HashMap<>();
            defaultConfig.put("camera_type", "builtin");
            defaultConfig.put("device_id", 0);
            defaultConfig.put("resolution", "1280x720");
            defaultConfig.put("is_active", true);
            return defaultConfig;
        }
    }

    /**
     * Get all camera configurations for a lecturer (history)
     */
    public List<Map<String, Object>> getCameraConfigurationHistory(String lecturerId) {
        String sql = """
            SELECT camera_type, device_id, resolution, is_active, created_at
            FROM camera_configurations
            WHERE lecturer_id = ?
            ORDER BY created_at DESC
        """;
        
        return jdbcTemplate.queryForList(sql, lecturerId);
    }

    /**
     * Deactivate a camera configuration
     */
    @Transactional
    public void deactivateCameraConfiguration(String lecturerId) {
        String sql = """
            UPDATE camera_configurations
            SET is_active = FALSE, updated_at = NOW()
            WHERE lecturer_id = ? AND is_active = TRUE
        """;
        
        jdbcTemplate.update(sql, lecturerId);
        logger.info("📷 Camera configuration deactivated for lecturer: {}", lecturerId);
    }

    /**
     * Start a camera session for a lecture
     */
    public CameraSession startCameraSession(String sessionId, String lecturerId) {
        Map<String, Object> config = getCameraConfiguration(lecturerId);
        
        String cameraType = (String) config.get("camera_type");
        int deviceId = (int) config.get("device_id");
        
        CameraConfigurationFactory.CameraConfiguration cameraConfig = 
            cameraFactory.createCamera(cameraType, String.valueOf(deviceId));
        
        VideoStreamReader reader = cameraConfig.getReader();
        boolean connected = reader.connect();
        
        if (connected) {
            CameraSession session = new CameraSession(sessionId, lecturerId, reader, cameraType, deviceId);
            activeCameras.put(sessionId, session);
            logger.info("🎥 Camera session started: {} for lecture {}", cameraType, sessionId);
            return session;
        } else {
            logger.error("❌ Failed to start camera session for lecture {}", sessionId);
            return null;
        }
    }

    /**
     * Stop a camera session
     */
    public void stopCameraSession(String sessionId) {
        CameraSession session = activeCameras.remove(sessionId);
        if (session != null && session.getReader() != null) {
            session.getReader().disconnect();
            logger.info("🛑 Camera session stopped: {}", sessionId);
        }
    }

    /**
     * Get active camera session
     */
    public CameraSession getActiveCameraSession(String sessionId) {
        return activeCameras.get(sessionId);
    }

    /**
     * Test if a camera is available
     */
    public boolean testCamera(String cameraType, int deviceId) {
        CameraConfigurationFactory.CameraConfiguration config = 
            cameraFactory.createCamera(cameraType, String.valueOf(deviceId));
        
        VideoStreamReader reader = config.getReader();
        boolean connected = reader.connect();
        
        if (connected) {
            reader.disconnect();
        }
        
        return connected;
    }

    /**
     * Get list of available cameras
     */
    public List<Map<String, Object>> getAvailableCameras() {
        List<Map<String, Object>> cameras = new ArrayList<>();
        
        // Check for built-in camera (device 0)
        if (testCamera("builtin", 0)) {
            Map<String, Object> cam = new HashMap<>();
            cam.put("id", 0);
            cam.put("name", "Built-in Camera");
            cam.put("type", "builtin");
            cam.put("available", true);
            cameras.add(cam);
        }
        
        // Check for USB webcams (devices 1-4)
        for (int i = 1; i <= 4; i++) {
            if (testCamera("usb", i)) {
                Map<String, Object> cam = new HashMap<>();
                cam.put("id", i);
                cam.put("name", i == 1 ? "USB Webcam" : "USB Camera " + i);
                cam.put("type", "usb");
                cam.put("available", true);
                cameras.add(cam);
            }
        }
        
        return cameras;
    }

    /**
     * Camera Session inner class
     */
    public static class CameraSession {
        private final String sessionId;
        private final String lecturerId;
        private final VideoStreamReader reader;
        private final String cameraType;
        private final int deviceId;
        private final Date startTime;
        
        public CameraSession(String sessionId, String lecturerId, VideoStreamReader reader, 
                            String cameraType, int deviceId) {
            this.sessionId = sessionId;
            this.lecturerId = lecturerId;
            this.reader = reader;
            this.cameraType = cameraType;
            this.deviceId = deviceId;
            this.startTime = new Date();
        }
        
        public String getSessionId() { return sessionId; }
        public String getLecturerId() { return lecturerId; }
        public VideoStreamReader getReader() { return reader; }
        public String getCameraType() { return cameraType; }
        public int getDeviceId() { return deviceId; }
        public Date getStartTime() { return startTime; }
        
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("sessionId", sessionId);
            map.put("lecturerId", lecturerId);
            map.put("cameraType", cameraType);
            map.put("deviceId", deviceId);
            map.put("startTime", startTime.toString());
            return map;
        }
    }
}