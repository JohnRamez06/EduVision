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
