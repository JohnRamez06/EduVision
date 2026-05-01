// src/main/java/com/eduvision/controller/ReportController.java
package com.eduvision.controller;

import com.eduvision.service.ReportService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/student/{studentId}/weekly/{weekId}")
    public ResponseEntity<Map<String, String>> generateStudentWeeklyReport(
            @PathVariable String studentId,
            @PathVariable String weekId) {
        String fileUrl = reportService.generateStudentWeeklyReport(studentId, weekId);
        return ResponseEntity.ok(Map.of(
                "message", "Student weekly report generated successfully.",
                "fileUrl", fileUrl,
                "status",  "ready"));
    }

    @PostMapping("/lecturer/{lecturerId}/weekly/{weekId}")
    public ResponseEntity<Map<String, String>> generateLecturerWeeklyReport(
            @PathVariable String lecturerId,
            @PathVariable String weekId) {
        String fileUrl = reportService.generateLecturerWeeklyReport(lecturerId, weekId);
        return ResponseEntity.ok(Map.of(
                "message", "Lecturer weekly report generated successfully.",
                "fileUrl", fileUrl,
                "status",  "ready"));
    }

    @PostMapping("/dean/weekly/{weekId}")
    public ResponseEntity<Map<String, String>> generateDeanWeeklyReport(
            @PathVariable String weekId) {
        String fileUrl = reportService.generateDeanWeeklyReport(weekId);
        return ResponseEntity.ok(Map.of(
                "message", "Dean weekly report generated successfully.",
                "fileUrl", fileUrl,
                "status",  "ready"));
    }

    @PostMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, String>> generateSessionReport(
            @PathVariable String sessionId) {
        String fileUrl = reportService.generateSessionReport(sessionId);
        return ResponseEntity.ok(Map.of(
                "message", "Session report generated successfully.",
                "fileUrl", fileUrl,
                "status",  "ready"));
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadReport(@PathVariable String fileName) {
        Resource resource = reportService.getReportFile(fileName);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }
}