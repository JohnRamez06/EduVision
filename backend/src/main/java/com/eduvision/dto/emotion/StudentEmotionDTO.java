package com.eduvision.dto.emotion;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StudentEmotionDTO {
    private String id;
    private String snapshotId;
    private String sessionId;
    private String studentId;
    private String emotion;
    private String concentration;  // Keep as String
    private BigDecimal confidenceScore;
    private String boundingBox;
    private String gazeDirection;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime capturedAt;
    private boolean anonymised;

    // Helper method to set concentration from double
    public void setConcentrationFromDouble(double value) {
        if (value >= 0.7) {
            this.concentration = "high";
        } else if (value >= 0.4) {
            this.concentration = "medium";
        } else {
            this.concentration = "low";
        }
    }
    
    // Helper method to set concentration from string
    public void setConcentrationFromString(String value) {
        this.concentration = value;
    }

    // Original getters/setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getEmotion() {
        return emotion;
    }

    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }

    public String getConcentration() {
        return concentration;
    }

    public void setConcentration(String concentration) {
        this.concentration = concentration;
    }

    public BigDecimal getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(BigDecimal confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(String boundingBox) {
        this.boundingBox = boundingBox;
    }

    public String getGazeDirection() {
        return gazeDirection;
    }

    public void setGazeDirection(String gazeDirection) {
        this.gazeDirection = gazeDirection;
    }

    public LocalDateTime getCapturedAt() {
        return capturedAt;
    }

    public void setCapturedAt(LocalDateTime capturedAt) {
        this.capturedAt = capturedAt;
    }

    public boolean isAnonymised() {
        return anonymised;
    }

    public void setAnonymised(boolean anonymised) {
        this.anonymised = anonymised;
    }
}