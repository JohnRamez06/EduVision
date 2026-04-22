package com.eduvision.controller;

import com.eduvision.dto.report.ReportRequestDTO;
import com.eduvision.dto.report.ReportStatusDTO;
import com.eduvision.service.ReportService;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/generate")
    public ResponseEntity<ReportStatusDTO> generate(@RequestBody ReportRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reportService.generateReport(request));
    }

    @GetMapping
    public ResponseEntity<List<ReportStatusDTO>> listUserReports(@RequestParam("userId") String userId) {
        return ResponseEntity.ok(reportService.getUserReports(userId));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<ReportStatusDTO> getStatus(@PathVariable String id) {
        return ResponseEntity.ok(reportService.getReportStatus(id));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable String id) {
        Resource report = reportService.downloadReport(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + report.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(report);
    }
}
