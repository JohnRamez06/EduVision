// src/main/java/com/eduvision/dto/alert/NotificationDTO.java
package com.eduvision.dto.alert;

import java.time.LocalDateTime;

public class NotificationDTO {
    private String id;
    private String userId;
    private String alertId;
    private String title;
    private String message;
    private String type;
    private boolean isRead;
    private LocalDateTime createdAt;

    public NotificationDTO() {}

    public NotificationDTO(String id, String userId, String alertId, String title, 
                          String message, String type, boolean isRead, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.alertId = alertId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getAlertId() { return alertId; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getType() { return type; }
    public boolean isRead() { return isRead; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setAlertId(String alertId) { this.alertId = alertId; }
    public void setTitle(String title) { this.title = title; }
    public void setMessage(String message) { this.message = message; }
    public void setType(String type) { this.type = type; }
    public void setRead(boolean isRead) { this.isRead = isRead; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}