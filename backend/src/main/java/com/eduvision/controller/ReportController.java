package com.eduvision.controller;

import com.eduvision.model.Report;
import com.eduvision.service.ReportService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/student/{studentId}/weekly/{weekId}")
    public ResponseEntity<Map<String, Object>> generateStudentWeeklyReport(
            @PathVariable String studentId, @PathVariable String weekId) {
        Report r = reportService.generateStudentWeeklyReport(studentId, weekId);
        return ResponseEntity.ok(toMap(r));
    }

    @PostMapping("/lecturer/{lecturerId}/weekly/{weekId}")
    public ResponseEntity<Map<String, Object>> generateLecturerWeeklyReport(
            @PathVariable String lecturerId, @PathVariable String weekId) {
        Report r = reportService.generateLecturerWeeklyReport(lecturerId, weekId);
        return ResponseEntity.ok(toMap(r));
    }

    @PostMapping("/dean/weekly/{weekId}")
    public ResponseEntity<Map<String, Object>> generateDeanWeeklyReport(
            @PathVariable String weekId) {
        Report r = reportService.generateDeanWeeklyReport(weekId);
        return ResponseEntity.ok(toMap(r));
    }

    @PostMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, Object>> generateSessionReport(
            @PathVariable String sessionId) {
        Report r = reportService.generateSessionReport(sessionId);
        return ResponseEntity.ok(toMap(r));
    }

    /**
     * POST /api/v1/reports/session/{sessionId}/compute-summaries
     *
     * Triggers R to compute student_lecture_summaries for all students in the session.
     * Called by the lecturer/system after a session ends.
     */
    @PostMapping("/session/{sessionId}/compute-summaries")
    public ResponseEntity<Map<String, String>> computeStudentSummaries(
            @PathVariable String sessionId) {
        reportService.computeStudentSummaries(sessionId);
        return ResponseEntity.ok(Map.of(
            "status",  "ok",
            "message", "Student summaries computed for session " + sessionId
        ));
    }

    /**
     * POST /api/v1/reports/my/session/{sessionId}
     *
     * Student generates their own session report PDF.
     */
    @PostMapping("/my/session/{sessionId}")
    public ResponseEntity<Map<String, Object>> generateMySessionReport(
            @PathVariable String sessionId) {
        Report r = reportService.generateMySessionReport(sessionId);
        return ResponseEntity.ok(toMap(r));
    }

    @GetMapping("/my")
    public ResponseEntity<List<Map<String, Object>>> getMyReports() {
        List<Map<String, Object>> reports = reportService.getMyReports()
                .stream().map(this::toMap).toList();
        return ResponseEntity.ok(reports);
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

    private Map<String, Object> toMap(Report r) {
        return Map.of(
            "id",          r.getId(),
            "title",       r.getTitle(),
            "type",        r.getType().name(),
            "status",      r.getStatus().name(),
            "fileUrl",     r.getFileUrl() != null ? r.getFileUrl() : "",
            "requestedAt", r.getRequestedAt().toString(),
            "completedAt", r.getCompletedAt() != null ? r.getCompletedAt().toString() : ""
        );
    }
}
