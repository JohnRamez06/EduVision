package com.eduvision.dto.alert;

import com.eduvision.model.AlertSeverity;
import java.time.LocalDateTime;

public class AlertDTO {
    private String id;
    private String type;
    private AlertSeverity severity;
    private String title;
    private String message;
    private LocalDateTime timestamp;
    private boolean acknowledged;

    public AlertDTO() {}

    public AlertDTO(String id, String type, AlertSeverity severity, String title, String message, LocalDateTime timestamp, boolean acknowledged) {
        this.id = id;
        this.type = type;
        this.severity = severity;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.acknowledged = acknowledged;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public AlertSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AlertSeverity severity) {
        this.severity = severity;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }
}
