// src/main/java/com/eduvision/dto/alert/AlertDTO.java
package com.eduvision.dto.alert;

import java.time.LocalDateTime;

public class AlertDTO {
    private String id;
    private String sessionId;
    private String courseId;
    private String title;
    private String message;
    private String severity;
    private String status;
    private String alertType;
    private LocalDateTime triggeredAt;
    private LocalDateTime acknowledgedAt;
    private String acknowledgedBy;

    public AlertDTO() {}

    // Getters
    public String getId() { return id; }
    public String getSessionId() { return sessionId; }
    public String getCourseId() { return courseId; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getSeverity() { return severity; }
    public String getStatus() { return status; }
    public String getAlertType() { return alertType; }
    public LocalDateTime getTriggeredAt() { return triggeredAt; }
    public LocalDateTime getAcknowledgedAt() { return acknowledgedAt; }
    public String getAcknowledgedBy() { return acknowledgedBy; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public void setTitle(String title) { this.title = title; }
    public void setMessage(String message) { this.message = message; }
    public void setSeverity(String severity) { this.severity = severity; }
    public void setStatus(String status) { this.status = status; }
    public void setAlertType(String alertType) { this.alertType = alertType; }
    public void setTriggeredAt(LocalDateTime triggeredAt) { this.triggeredAt = triggeredAt; }
    public void setAcknowledgedAt(LocalDateTime acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }
    public void setAcknowledgedBy(String acknowledgedBy) { this.acknowledgedBy = acknowledgedBy; }
}