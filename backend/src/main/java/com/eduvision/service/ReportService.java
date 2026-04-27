package com.eduvision.service;

import com.eduvision.dto.report.ReportRequestDTO;
import com.eduvision.dto.report.ReportStatusDTO;
import com.eduvision.exception.ResourceNotFoundException;
import com.eduvision.model.Report;
import com.eduvision.model.ReportStatus;
import com.eduvision.model.ReportType;
import com.eduvision.model.User;
import com.eduvision.repository.ReportRepository;
import com.eduvision.repository.SessionRepository;
import com.eduvision.repository.UserRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final RScriptExecutor rScriptExecutor;

    /** Base directory of the analytics-r folder (configurable via application.properties). */
    @Value("${eduvision.analytics.base-path:analytics-r}")
    private String analyticsBasePath;

    public ReportService(ReportRepository reportRepository,
                         UserRepository userRepository,
                         SessionRepository sessionRepository,
                         RScriptExecutor rScriptExecutor) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.rScriptExecutor = rScriptExecutor;
    }

    @Transactional
    public ReportStatusDTO generateReport(ReportRequestDTO request) {
        User requestedBy = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getUserId()));

        Report report = new Report();
        report.setId(UUID.randomUUID().toString());
        report.setType(request.getType() != null ? request.getType() : ReportType.custom);
        report.setStatus(ReportStatus.generating);
        report.setTitle(request.getTitle() != null ? request.getTitle() : "Generated Report");
        report.setDescription(request.getDescription());
        report.setRequestedBy(requestedBy);
        report.setRequestedAt(LocalDateTime.now());
        report.setDateFrom(request.getDateFrom());
        report.setDateTo(request.getDateTo());

        if (request.getSessionId() != null) {
            report.setSession(sessionRepository.findById(request.getSessionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + request.getSessionId())));
        }
        if (request.getStudentId() != null) {
            report.setStudent(userRepository.findById(request.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + request.getStudentId())));
        }

        report = reportRepository.save(report);

        try {
            String scriptPath = resolveScriptPath(request);
            List<String> args = buildScriptArgs(request, report.getId());

            report.setRScriptUsed(scriptPath);
            String output = rScriptExecutor.execute(scriptPath, args);

            report.setStatus(ReportStatus.ready);
            report.setCompletedAt(LocalDateTime.now());
            report.setResultMetadata(output);

            String firstLine = output.lines().findFirst().orElse("").trim();
            if (!firstLine.isBlank() && Files.exists(Path.of(firstLine))) {
                report.setFileUrl(firstLine);
                report.setFileSizeBytes(Files.size(Path.of(firstLine)));
            }
        } catch (Exception ex) {
            report.setStatus(ReportStatus.failed);
            report.setCompletedAt(LocalDateTime.now());
            report.setResultMetadata(ex.getMessage());
        }

        return toDto(reportRepository.save(report));
    }

    /**
     * Determine which R generator script to run based on the report type.
     * Falls back to any caller-supplied scriptPath, then to a default.
     */
    private String resolveScriptPath(ReportRequestDTO request) {
        if (request.getScriptPath() != null && !request.getScriptPath().isBlank()) {
            return request.getScriptPath();
        }
        if (request.getType() == null) {
            return analyticsBasePath + "/generators/generate_session_report.R";
        }
        return switch (request.getType()) {
            case weekly_student  -> analyticsBasePath + "/generators/generate_student_weekly.R";
            case weekly_lecturer -> analyticsBasePath + "/generators/generate_lecturer_weekly.R";
            case weekly_dean     -> analyticsBasePath + "/generators/generate_dean_weekly.R";
            case session_summary -> analyticsBasePath + "/generators/generate_session_report.R";
            case course_analytics -> analyticsBasePath + "/generators/generate_course_report.R";
            case comparison      -> analyticsBasePath + "/generators/generate_comparison_report.R";
            default              -> analyticsBasePath + "/generators/generate_session_report.R";
        };
    }

    /**
     * Build the positional argument list that the selected R script expects.
     * Convention for each generator:
     *   weekly_student  : <student_id> <week_id> [output_dir]
     *   weekly_lecturer : <lecturer_id> <week_id> [output_dir]
     *   weekly_dean     : <dean_id> <week_id> [output_dir]
     *   session_summary : <session_id> [output_dir]
     *   course_analytics: <course_id> [output_dir]
     *   comparison      : <lecturers|courses> <ids_csv> [output_dir]
     *   custom / other  : <report_id> [session_id]
     */
    private List<String> buildScriptArgs(ReportRequestDTO request, String reportId) {
        String outputDir = analyticsBasePath + "/output";
        List<String> args = new ArrayList<>();

        if (request.getType() == null) {
            args.add(reportId);
            if (request.getSessionId() != null) args.add(request.getSessionId());
            return args;
        }

        switch (request.getType()) {
            case weekly_student -> {
                requireField(request.getStudentId(), "studentId is required for weekly_student report");
                requireField(request.getWeekId(), "weekId is required for weekly_student report");
                args.add(request.getStudentId());
                args.add(request.getWeekId());
                args.add(outputDir);
            }
            case weekly_lecturer -> {
                String lid = request.getLecturerId() != null ? request.getLecturerId() : request.getUserId();
                requireField(lid, "lecturerId is required for weekly_lecturer report");
                requireField(request.getWeekId(), "weekId is required for weekly_lecturer report");
                args.add(lid);
                args.add(request.getWeekId());
                args.add(outputDir);
            }
            case weekly_dean -> {
                requireField(request.getWeekId(), "weekId is required for weekly_dean report");
                args.add(request.getUserId());
                args.add(request.getWeekId());
                args.add(outputDir);
            }
            case session_summary -> {
                requireField(request.getSessionId(), "sessionId is required for session_summary report");
                args.add(request.getSessionId());
                args.add(outputDir);
            }
            case course_analytics -> {
                requireField(request.getCourseId(), "courseId is required for course_analytics report");
                args.add(request.getCourseId());
                args.add(outputDir);
            }
            case comparison -> {
                requireField(request.getCompareIds(), "compareIds is required for comparison report");
                // Determine type: if compareIds looks like lecturer IDs vs course IDs
                // The caller sets compareIds as "lecturers:<id1,id2>" or "courses:<id1,id2>"
                String raw = request.getCompareIds();
                String compareType = raw.startsWith("courses:") ? "courses" : "lecturers";
                String ids = raw.contains(":") ? raw.substring(raw.indexOf(':') + 1) : raw;
                args.add(compareType);
                args.add(ids);
                args.add(outputDir);
            }
            default -> {
                args.add(reportId);
                if (request.getSessionId() != null) args.add(request.getSessionId());
            }
        }
        return args;
    }

    private static void requireField(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    @Transactional(readOnly = true)
    public ReportStatusDTO getReportStatus(String reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found: " + reportId));
        return toDto(report);
    }

    @Transactional(readOnly = true)
    public Resource downloadReport(String reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found: " + reportId));

        if (report.getFileUrl() == null || report.getFileUrl().isBlank()) {
            throw new ResourceNotFoundException("Report file is not ready yet");
        }

        FileSystemResource resource = new FileSystemResource(Path.of(report.getFileUrl()));
        if (!resource.exists()) {
            throw new ResourceNotFoundException("Report file not found on disk");
        }
        return resource;
    }

    @Transactional(readOnly = true)
    public List<ReportStatusDTO> getUserReports(String userId) {
        return reportRepository.findByRequestedByIdOrderByRequestedAtDesc(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private ReportStatusDTO toDto(Report report) {
        ReportStatusDTO dto = new ReportStatusDTO();
        dto.setReportId(report.getId());
        dto.setTitle(report.getTitle());
        dto.setType(report.getType());
        dto.setStatus(report.getStatus());
        dto.setFileUrl(report.getFileUrl());
        dto.setFileSizeBytes(report.getFileSizeBytes());
        dto.setResultMetadata(report.getResultMetadata());
        dto.setRequestedAt(report.getRequestedAt());
        dto.setCompletedAt(report.getCompletedAt());
        return dto;
    }
}


@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final RScriptExecutor rScriptExecutor;

    public ReportService(ReportRepository reportRepository,
                         UserRepository userRepository,
                         SessionRepository sessionRepository,
                         RScriptExecutor rScriptExecutor) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.rScriptExecutor = rScriptExecutor;
    }

    @Transactional
    public ReportStatusDTO generateReport(ReportRequestDTO request) {
        User requestedBy = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getUserId()));

        Report report = new Report();
        report.setId(UUID.randomUUID().toString());
        report.setType(request.getType() != null ? request.getType() : ReportType.custom);
        report.setStatus(ReportStatus.generating);
        report.setTitle(request.getTitle() != null ? request.getTitle() : "Generated Report");
        report.setDescription(request.getDescription());
        report.setRequestedBy(requestedBy);
        report.setRequestedAt(LocalDateTime.now());
        report.setDateFrom(request.getDateFrom());
        report.setDateTo(request.getDateTo());

        if (request.getSessionId() != null) {
            report.setSession(sessionRepository.findById(request.getSessionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + request.getSessionId())));
        }
        if (request.getStudentId() != null) {
            report.setStudent(userRepository.findById(request.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + request.getStudentId())));
        }

        report = reportRepository.save(report);

        try {
            String scriptPath = request.getScriptPath() != null ? request.getScriptPath() : "reports/generate_report.R";
            List<String> args = new ArrayList<>();
            args.add(report.getId());
            if (request.getSessionId() != null) {
                args.add(request.getSessionId());
            }
            String output = rScriptExecutor.execute(scriptPath, args);

            report.setStatus(ReportStatus.ready);
            report.setCompletedAt(LocalDateTime.now());
            report.setResultMetadata(output);

            String firstLine = output.lines().findFirst().orElse("").trim();
            if (!firstLine.isBlank() && Files.exists(Path.of(firstLine))) {
                report.setFileUrl(firstLine);
                report.setFileSizeBytes(Files.size(Path.of(firstLine)));
            }
        } catch (Exception ex) {
            report.setStatus(ReportStatus.failed);
            report.setCompletedAt(LocalDateTime.now());
            report.setResultMetadata(ex.getMessage());
        }

        return toDto(reportRepository.save(report));
    }

    @Transactional(readOnly = true)
    public ReportStatusDTO getReportStatus(String reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found: " + reportId));
        return toDto(report);
    }

    @Transactional(readOnly = true)
    public Resource downloadReport(String reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found: " + reportId));

        if (report.getFileUrl() == null || report.getFileUrl().isBlank()) {
            throw new ResourceNotFoundException("Report file is not ready yet");
        }

        FileSystemResource resource = new FileSystemResource(Path.of(report.getFileUrl()));
        if (!resource.exists()) {
            throw new ResourceNotFoundException("Report file not found on disk");
        }
        return resource;
    }

    @Transactional(readOnly = true)
    public List<ReportStatusDTO> getUserReports(String userId) {
        return reportRepository.findByRequestedByIdOrderByRequestedAtDesc(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private ReportStatusDTO toDto(Report report) {
        ReportStatusDTO dto = new ReportStatusDTO();
        dto.setReportId(report.getId());
        dto.setTitle(report.getTitle());
        dto.setType(report.getType());
        dto.setStatus(report.getStatus());
        dto.setFileUrl(report.getFileUrl());
        dto.setFileSizeBytes(report.getFileSizeBytes());
        dto.setResultMetadata(report.getResultMetadata());
        dto.setRequestedAt(report.getRequestedAt());
        dto.setCompletedAt(report.getCompletedAt());
        return dto;
    }
}
