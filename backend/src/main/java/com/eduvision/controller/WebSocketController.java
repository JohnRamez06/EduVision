package com.eduvision.controller;

import com.eduvision.service.AlertService;
import com.eduvision.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private AlertService alertService;

    @Autowired
    private AttendanceService attendanceService;

    private final Map<String, LiveSessionInfo> activeSessions = new ConcurrentHashMap<>();

    @MessageMapping("/session.start")
    @SendTo("/topic/session.started")
    public SessionStatus startSession(@RequestBody SessionStartRequest request) {
        LiveSessionInfo session = new LiveSessionInfo();
        session.setSessionId(request.getSessionId());
        session.setCourseId(request.getCourseId());
        session.setLecturerId(request.getLecturerId());
        session.setStartTime(LocalDateTime.now());
        activeSessions.put(request.getSessionId(), session);
        
        SessionStatus status = new SessionStatus();
        status.setSuccess(true);
        status.setSessionId(request.getSessionId());
        status.setMessage("Session started successfully");
        return status;
    }

    @MessageMapping("/session.end")
    @SendTo("/topic/session.ended")
    public SessionStatus endSession(@RequestBody SessionEndRequest request) {
        activeSessions.remove(request.getSessionId());
        SessionStatus status = new SessionStatus();
        status.setSuccess(true);
        status.setSessionId(request.getSessionId());
        status.setMessage("Session ended successfully");
        return status;
    }

    @PostMapping("/api/websocket/face-update/{sessionId}")
    public void sendFaceUpdate(@PathVariable String sessionId, @RequestBody FaceDetectionData detection) {
        detection.setTimestamp(LocalDateTime.now().toString());
        messagingTemplate.convertAndSend("/topic/session." + sessionId + ".faces", detection);
        
        // Check for low engagement alert (engagement score < 20%)
        if (detection.getEngagementScore() < 20) {
            String title = "Low Engagement Detected";
            String message = detection.getStudentName() + " has low engagement (" + 
                           Math.round(detection.getEngagementScore()) + "%)";
            String severity = "warning";
            String alertType = "LOW_ENGAGEMENT";
            
            // Use existing createAlert method
            Map<String, Object> alert = alertService.createAlert(sessionId, title, message, severity, alertType);
            
            // Send alert via WebSocket with the same format
            AlertMessage alertMsg = new AlertMessage();
            alertMsg.setId((String) alert.get("id"));
            alertMsg.setSessionId(sessionId);
            alertMsg.setTitle(title);
            alertMsg.setMessage(message);
            alertMsg.setSeverity(severity);
            alertMsg.setType(alertType);
            alertMsg.setStudentId(detection.getStudentId());
            alertMsg.setStudentName(detection.getStudentName());
            alertMsg.setEngagementScore(detection.getEngagementScore());
            alertMsg.setTimestamp(LocalDateTime.now().toString());
            
            sendAlert(sessionId, alertMsg);
        }
        
        // Check for high confusion (if you have confusion detection)
        if (detection.getEmotion() != null && detection.getEmotion().equalsIgnoreCase("confused")) {
            String title = "High Confusion Detected";
            String message = detection.getStudentName() + " appears confused";
            String severity = "info";
            String alertType = "HIGH_CONFUSION";
            
            Map<String, Object> alert = alertService.createAlert(sessionId, title, message, severity, alertType);
            
            AlertMessage alertMsg = new AlertMessage();
            alertMsg.setId((String) alert.get("id"));
            alertMsg.setSessionId(sessionId);
            alertMsg.setTitle(title);
            alertMsg.setMessage(message);
            alertMsg.setSeverity(severity);
            alertMsg.setType(alertType);
            alertMsg.setStudentId(detection.getStudentId());
            alertMsg.setStudentName(detection.getStudentName());
            alertMsg.setTimestamp(LocalDateTime.now().toString());
            
            sendAlert(sessionId, alertMsg);
        }
    }

    @PostMapping("/api/websocket/engagement-update/{sessionId}")
    public void sendEngagementUpdate(@PathVariable String sessionId, @RequestBody EngagementData engagement) {
        messagingTemplate.convertAndSend("/topic/session." + sessionId + ".engagement", engagement);
        
        // Check class-level engagement alert
        if (engagement.getOverall() < 20) {
            String title = "Critical: Very Low Class Engagement";
            String message = "Overall class engagement is only " + Math.round(engagement.getOverall()) + "%";
            String severity = "critical";
            String alertType = "CLASS_LOW_ENGAGEMENT";
            
            Map<String, Object> alert = alertService.createAlert(sessionId, title, message, severity, alertType);
            
            AlertMessage alertMsg = new AlertMessage();
            alertMsg.setId((String) alert.get("id"));
            alertMsg.setSessionId(sessionId);
            alertMsg.setTitle(title);
            alertMsg.setMessage(message);
            alertMsg.setSeverity(severity);
            alertMsg.setType(alertType);
            alertMsg.setTimestamp(LocalDateTime.now().toString());
            
            sendAlert(sessionId, alertMsg);
        }
    }

    @PostMapping("/api/websocket/attendance-update/{sessionId}")
    public void sendAttendanceUpdate(@PathVariable String sessionId, @RequestBody AttendanceUpdate attendance) {
        messagingTemplate.convertAndSend("/topic/session." + sessionId + ".attendance", attendance);
    }

    public void sendAlert(String sessionId, AlertMessage alert) {
        messagingTemplate.convertAndSend("/topic/session." + sessionId + ".alerts", alert);
    }
    
    // Endpoint to get alerts for a session (REST fallback)
    @GetMapping("/api/sessions/{sessionId}/alerts")
    public List<Map<String, Object>> getSessionAlerts(@PathVariable String sessionId) {
        return alertService.getAlertsForSession(sessionId);
    }
    
    // Endpoint to acknowledge an alert
    @PostMapping("/api/alerts/{alertId}/acknowledge")
    public void acknowledgeAlert(@PathVariable String alertId) {
        alertService.markAlertAcknowledged(alertId);
    }
}

// ========== DTO Classes ==========

class LiveSessionInfo {
    private String sessionId;
    private Long courseId;
    private Long lecturerId;
    private LocalDateTime startTime;
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public Long getLecturerId() { return lecturerId; }
    public void setLecturerId(Long lecturerId) { this.lecturerId = lecturerId; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
}

class SessionStartRequest {
    private String sessionId;
    private Long courseId;
    private Long lecturerId;
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public Long getLecturerId() { return lecturerId; }
    public void setLecturerId(Long lecturerId) { this.lecturerId = lecturerId; }
}

class SessionEndRequest {
    private String sessionId;
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
}

class SessionStatus {
    private boolean success;
    private String sessionId;
    private String message;
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

class FaceDetectionData {
    private String studentId;
    private String studentName;
    private String emotion;
    private double confidence;
    private double engagementScore;
    private String concentration;
    private String timestamp;
    private double[] bbox;
    
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getEmotion() { return emotion; }
    public void setEmotion(String emotion) { this.emotion = emotion; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    public double getEngagementScore() { return engagementScore; }
    public void setEngagementScore(double engagementScore) { this.engagementScore = engagementScore; }
    public String getConcentration() { return concentration; }
    public void setConcentration(String concentration) { this.concentration = concentration; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public double[] getBbox() { return bbox; }
    public void setBbox(double[] bbox) { this.bbox = bbox; }
}

class EngagementData {
    private double overall;
    private int high;
    private int medium;
    private int low;
    private double avgEngagement;
    
    public double getOverall() { return overall; }
    public void setOverall(double overall) { this.overall = overall; }
    public int getHigh() { return high; }
    public void setHigh(int high) { this.high = high; }
    public int getMedium() { return medium; }
    public void setMedium(int medium) { this.medium = medium; }
    public int getLow() { return low; }
    public void setLow(int low) { this.low = low; }
    public double getAvgEngagement() { return avgEngagement; }
    public void setAvgEngagement(double avgEngagement) { this.avgEngagement = avgEngagement; }
}

class AttendanceUpdate {
    private String studentId;
    private String studentName;
    private String status;
    private String timestamp;
    
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}

class AlertMessage {
    private String id;
    private String sessionId;
    private String studentId;
    private String studentName;
    private String title;
    private String message;
    private String severity;
    private String type;
    private Double engagementScore;
    private String timestamp;
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Double getEngagementScore() { return engagementScore; }
    public void setEngagementScore(Double engagementScore) { this.engagementScore = engagementScore; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}