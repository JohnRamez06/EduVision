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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
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

    @Value("${eduvision.reports.storage-path:/tmp/eduvision-reports}")
    private String reportsStoragePath;

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
        User requestedBy = userRepository.findById(request.getRequestedById())
                .orElseThrow(() -> new ResourceNotFoundException("Requested user not found: " + request.getRequestedById()));

        Report report = new Report();
        report.setId(UUID.randomUUID().toString());
        report.setType(request.getType() == null ? ReportType.custom : request.getType());
        report.setStatus(ReportStatus.generating);
        report.setTitle(request.getTitle() == null ? "Generated report" : request.getTitle());
        report.setDescription(request.getDescription());
        report.setRequestedBy(requestedBy);
        report.setDateFrom(request.getDateFrom());
        report.setDateTo(request.getDateTo());
        report.setRequestedAt(LocalDateTime.now());
        report.setRScriptUsed(request.getScriptPath());
        if (request.getSessionId() != null) {
            report.setSession(sessionRepository.findById(request.getSessionId()).orElse(null));
        }

        report = reportRepository.save(report);
        RScriptExecutor.ExecutionResult executionResult =
                rScriptExecutor.execute(resolveScriptPath(request), request.getScriptArgs());

        report.setResultMetadata(buildMetadata(executionResult));
        if (executionResult.getExitCode() == 0) {
            persistOutputFile(report, executionResult.getStdout());
            report.setStatus(ReportStatus.ready);
            report.setCompletedAt(LocalDateTime.now());
        } else {
            report.setStatus(ReportStatus.failed);
        }

        return toStatusDto(reportRepository.save(report));
    }

    @Transactional(readOnly = true)
    public ReportStatusDTO getReportStatus(String reportId) {
        return toStatusDto(getReport(reportId));
    }

    @Transactional(readOnly = true)
    public Resource downloadReport(String reportId) {
        Report report = getReport(reportId);
        if (report.getFileUrl() == null) {
            throw new ResourceNotFoundException("Report file not available: " + reportId);
        }

        String fileName = report.getFileUrl().replace("/reports/", "");
        Path filePath = Paths.get(reportsStoragePath).resolve(fileName);
        return new FileSystemResource(filePath);
    }

    @Transactional(readOnly = true)
    public List<ReportStatusDTO> getUserReports(String userId) {
        return reportRepository.findByRequestedByIdOrderByRequestedAtDesc(userId)
                .stream()
                .map(this::toStatusDto)
                .toList();
    }

    private String resolveScriptPath(ReportRequestDTO request) {
        if (request.getScriptPath() != null && !request.getScriptPath().isBlank()) {
            return request.getScriptPath();
        }
        return "report_generator.R";
    }

    private void persistOutputFile(Report report, String content) {
        try {
            Path folder = Paths.get(reportsStoragePath);
            Files.createDirectories(folder);
            Path reportPath = folder.resolve(report.getId() + ".txt");
            Files.writeString(reportPath, content == null ? "" : content, StandardCharsets.UTF_8);
            report.setFileSizeBytes(Files.size(reportPath));
            report.setFileUrl("/reports/" + reportPath.getFileName());
        } catch (IOException exception) {
            report.setStatus(ReportStatus.failed);
            report.setResultMetadata("{\"error\":\"" + exception.getMessage() + "\"}");
        }
    }

    private String buildMetadata(RScriptExecutor.ExecutionResult result) {
        return "{\"exitCode\":" + result.getExitCode()
                + ",\"stdout\":\"" + escapeJson(result.getStdout())
                + "\",\"stderr\":\"" + escapeJson(result.getStderr()) + "\"}";
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private Report getReport(String reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found: " + reportId));
    }

    private ReportStatusDTO toStatusDto(Report report) {
        ReportStatusDTO dto = new ReportStatusDTO();
        dto.setReportId(report.getId());
        dto.setStatus(report.getStatus());
        dto.setFileUrl(report.getFileUrl());
        dto.setFileSizeBytes(report.getFileSizeBytes());
        dto.setRequestedAt(report.getRequestedAt());
        dto.setCompletedAt(report.getCompletedAt());
        return dto;
    }
}
