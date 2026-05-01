// src/main/java/com/eduvision/service/ReportService.java
package com.eduvision.service;

import com.eduvision.service.RScriptExecutor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;

@Service
public class ReportService {

    private static final String OUTPUT_BASE = "analytics-r/output";

    private final RScriptExecutor rScriptExecutor;

    public ReportService(RScriptExecutor rScriptExecutor) {
        this.rScriptExecutor = rScriptExecutor;
    }

    public String generateStudentWeeklyReport(String studentId, String weekId) {
        String fileName = "student_" + studentId + "_week_" + weekId + ".pdf";
        rScriptExecutor.execute("generators/generate_student_weekly.R", studentId, weekId);
        return "/api/v1/reports/download/" + fileName;
    }

    public String generateLecturerWeeklyReport(String lecturerId, String weekId) {
        String fileName = "lecturer_" + lecturerId + "_week_" + weekId + ".pdf";
        rScriptExecutor.execute("generators/generate_lecturer_weekly.R", lecturerId, weekId);
        return "/api/v1/reports/download/" + fileName;
    }

    public String generateDeanWeeklyReport(String weekId) {
        String fileName = "dean_week_" + weekId + ".pdf";
        rScriptExecutor.execute("generators/generate_dean_weekly.R", weekId);
        return "/api/v1/reports/download/" + fileName;
    }

    public String generateSessionReport(String sessionId) {
        String fileName = "session_" + sessionId + ".pdf";
        rScriptExecutor.execute("generators/generate_session_report.R", sessionId);
        return "/api/v1/reports/download/" + fileName;
    }

    public Resource getReportFile(String fileName) {
        // Try student/ and session/ subdirectories, then root output
        for (String sub : new String[]{"student/", "session/", "lecturer/", "dean/", ""}) {
            var path = Paths.get(OUTPUT_BASE, sub + fileName);
            if (path.toFile().exists()) {
                return new FileSystemResource(path.toFile());
            }
        }
        throw new RuntimeException("Report file not found: " + fileName);
    }
}