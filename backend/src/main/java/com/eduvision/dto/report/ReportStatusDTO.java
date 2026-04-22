package com.eduvision.dto.report;

import com.eduvision.model.ReportStatus;
import com.eduvision.model.ReportType;
import java.time.LocalDateTime;

public class ReportStatusDTO {
    private String reportId;
    private String title;
    private ReportType type;
    private ReportStatus status;
    private String fileUrl;
    private Long fileSizeBytes;
    private String resultMetadata;
    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ReportType getType() {
        return type;
    }

    public void setType(ReportType type) {
        this.type = type;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public String getResultMetadata() {
        return resultMetadata;
    }

    public void setResultMetadata(String resultMetadata) {
        this.resultMetadata = resultMetadata;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
