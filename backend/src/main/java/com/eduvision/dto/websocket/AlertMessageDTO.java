package com.eduvision.dto.websocket;

import com.eduvision.model.AlertSeverity;

import java.time.LocalDateTime;

public class AlertMessageDTO {

    /**
     * Logical type of alert:
     * "ENGAGEMENT_DROP" | "CONCENTRATION_LOW" | "STUDENT_AT_RISK"
     * | "EMOTION_ALERT" | "SYSTEM"
     */
    private String        type;
    private String        title;
    private String        message;
    private AlertSeverity severity;    // existing enum: info / warning / critical
    private String        sessionId;   // which session triggered this alert
    private LocalDateTime timestamp;

    public AlertMessageDTO() {}

    public AlertMessageDTO(String type, String title, String message,
                            AlertSeverity severity, String sessionId) {
        this.type      = type;
        this.title     = title;
        this.message   = message;
        this.severity  = severity;
        this.sessionId = sessionId;
        this.timestamp = LocalDateTime.now();
    }

    // Getters & Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public AlertSeverity getSeverity() { return severity; }
    public void setSeverity(AlertSeverity severity) { this.severity = severity; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}