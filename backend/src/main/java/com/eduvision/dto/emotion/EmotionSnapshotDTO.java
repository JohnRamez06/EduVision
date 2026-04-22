package com.eduvision.dto.emotion;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class EmotionSnapshotDTO {
    private String snapshotId;
    private String sessionId;
    private String cameraId;
    private Long seqIndex;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime capturedAt;
    private String frameUrl;
    private Integer studentCount;
    private BigDecimal avgConcentration;
    private String dominantEmotion;
    private BigDecimal engagementScore;
    private String rawPayload;
    private Integer processingMs;

    public String getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getCameraId() {
        return cameraId;
    }

    public void setCameraId(String cameraId) {
        this.cameraId = cameraId;
    }

    public Long getSeqIndex() {
        return seqIndex;
    }

    public void setSeqIndex(Long seqIndex) {
        this.seqIndex = seqIndex;
    }

    public LocalDateTime getCapturedAt() {
        return capturedAt;
    }

    public void setCapturedAt(LocalDateTime capturedAt) {
        this.capturedAt = capturedAt;
    }

    public String getFrameUrl() {
        return frameUrl;
    }

    public void setFrameUrl(String frameUrl) {
        this.frameUrl = frameUrl;
    }

    public Integer getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
    }

    public BigDecimal getAvgConcentration() {
        return avgConcentration;
    }

    public void setAvgConcentration(BigDecimal avgConcentration) {
        this.avgConcentration = avgConcentration;
    }

    public String getDominantEmotion() {
        return dominantEmotion;
    }

    public void setDominantEmotion(String dominantEmotion) {
        this.dominantEmotion = dominantEmotion;
    }

    public BigDecimal getEngagementScore() {
        return engagementScore;
    }

    public void setEngagementScore(BigDecimal engagementScore) {
        this.engagementScore = engagementScore;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public void setRawPayload(String rawPayload) {
        this.rawPayload = rawPayload;
    }

    public Integer getProcessingMs() {
        return processingMs;
    }

    public void setProcessingMs(Integer processingMs) {
        this.processingMs = processingMs;
    }
}
