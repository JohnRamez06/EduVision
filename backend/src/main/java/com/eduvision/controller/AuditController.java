                                                                                                                                                                  package com.eduvision.controller;

import com.eduvision.dto.audit.AuditLogDTO;
import com.eduvision.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/audit")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("/")
    public List<AuditLogDTO> getAllLogs() {
        return auditLogService.getAllLogs();
    }

    @GetMapping("/user/{userId}")
    public List<AuditLogDTO> getUserLogs(@PathVariable String userId) {
        return auditLogService.getUserLogs(userId);
    }

    @GetMapping("/resource/{type}/{id}")
    public List<AuditLogDTO> getResourceLogs(@PathVariable String type, @PathVariable String id) {
        return auditLogService.getResourceLogs(type, id);
    }

    @GetMapping("/export")
    public String exportLogs() {
        return auditLogService.exportLogs();
    }
}