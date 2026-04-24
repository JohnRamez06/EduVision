package com.eduvision.service;

import com.eduvision.dto.audit.AuditLogDTO;
import com.eduvision.model.AuditAction;
import com.eduvision.model.AuditLog;
import com.eduvision.model.User;
import com.eduvision.repository.AuditLogRepository;
import com.eduvision.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    public void logAction(String userId, String sessionId, AuditAction action, String resourceType, String resourceId,
                         String oldValue, String newValue, String ipAddress, String userAgent, boolean success) {
        AuditLog log = new AuditLog();
        if (userId != null) {
            userRepository.findById(userId).ifPresent(log::setUser);
        }
        // sessionId would need LectureSession repo, but for now skip or add later
        log.setAction(action);
        log.setResourceType(resourceType);
        log.setResourceId(resourceId);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setIpAddress(ipAddress);
        log.setUserAgent(userAgent);
        log.setSuccess(success);
        log.setOccurredAt(LocalDateTime.now());
        auditLogRepository.save(log);
    }

    public List<AuditLogDTO> getAllLogs() {
        return auditLogRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public List<AuditLogDTO> getResourceLogs(String resourceType, String resourceId) {
        return auditLogRepository.findByResourceTypeAndResourceId(resourceType, resourceId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public String exportLogs() {
        // Simple CSV export
        List<AuditLog> logs = auditLogRepository.findAll();
        StringBuilder csv = new StringBuilder("id,userId,action,resourceType,resourceId,ipAddress,occurredAt\n");
        for (AuditLog log : logs) {
            csv.append(log.getId()).append(",")
               .append(log.getUser() != null ? log.getUser().getId() : "").append(",")
               .append(log.getAction()).append(",")
               .append(log.getResourceType()).append(",")
               .append(log.getResourceId()).append(",")
               .append(log.getIpAddress()).append(",")
               .append(log.getOccurredAt()).append("\n");
        }
        return csv.toString();
    }

    private AuditLogDTO convertToDTO(AuditLog log) {
        return new AuditLogDTO(
            log.getId(),
            log.getUser() != null ? log.getUser().getId() : null,
            log.getAction().toString(),
            log.getResourceType(),
            log.getResourceId(),
            log.getIpAddress(),
            log.getOccurredAt()
        );
    }

    public List<AuditLogDTO> getUserLogs(String userId) {
    return auditLogRepository.findAll().stream()
            .filter(log -> log.getUser() != null &&
                           log.getUser().getId().equals(userId))
            .map(this::convertToDTO)
            .collect(Collectors.toList());
}
}