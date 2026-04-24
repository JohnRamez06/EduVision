package com.eduvision.dto.audit;

import java.time.LocalDateTime;

public class AuditLogDTO {
    private Long id;
    private String userId;
    private String action;
    private String resourceType;
    private String resourceId;
    private String ipAddress;
    private LocalDateTime occurredAt;

    // Constructors, getters, setters
    public AuditLogDTO() {}

    public AuditLogDTO(Long id, String userId, String action, String resourceType, String resourceId, String ipAddress, LocalDateTime occurredAt) {
        this.id = id;
        this.userId = userId;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.ipAddress = ipAddress;
        this.occurredAt = occurredAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }

    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
}