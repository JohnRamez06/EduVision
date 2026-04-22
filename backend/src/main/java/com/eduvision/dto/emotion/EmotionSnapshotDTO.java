package com.eduvision.dto.emotion;

import com.eduvision.model.EmotionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class EmotionSnapshotDTO {

    private String id;
    private String sessionId;
    private String cameraId;
    private Long seqIndex;
    private LocalDateTime capturedAt;
    private String frameUrl;
    private short studentCount;
    private BigDecimal avgConcentration;
    private EmotionType dominantEmotion;
    private BigDecimal engagementScore;
    private String rawPayload;
    private Integer processingMs;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getCameraId() { return cameraId; }
    public void setCameraId(String cameraId) { this.cameraId = cameraId; }
    public Long getSeqIndex() { return seqIndex; }
    public void setSeqIndex(Long seqIndex) { this.seqIndex = seqIndex; }
    public LocalDateTime getCapturedAt() { return capturedAt; }
    public void setCapturedAt(LocalDateTime capturedAt) { this.capturedAt = capturedAt; }
    public String getFrameUrl() { return frameUrl; }
    public void setFrameUrl(String frameUrl) { this.frameUrl = frameUrl; }
    public short getStudentCount() { return studentCount; }
    public void setStudentCount(short studentCount) { this.studentCount = studentCount; }
    public BigDecimal getAvgConcentration() { return avgConcentration; }
    public void setAvgConcentration(BigDecimal avgConcentration) { this.avgConcentration = avgConcentration; }
    public EmotionType getDominantEmotion() { return dominantEmotion; }
    public void setDominantEmotion(EmotionType dominantEmotion) { this.dominantEmotion = dominantEmotion; }
    public BigDecimal getEngagementScore() { return engagementScore; }
    public void setEngagementScore(BigDecimal engagementScore) { this.engagementScore = engagementScore; }
    public String getRawPayload() { return rawPayload; }
    public void setRawPayload(String rawPayload) { this.rawPayload = rawPayload; }
    public Integer getProcessingMs() { return processingMs; }
    public void setProcessingMs(Integer processingMs) { this.processingMs = processingMs; }
}
