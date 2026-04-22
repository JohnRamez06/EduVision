package com.eduvision.dto.emotion;

import com.eduvision.model.ConcentrationLevel;
import com.eduvision.model.EmotionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StudentEmotionDTO {

    private String id;
    private String snapshotId;
    private String sessionId;
    private String studentId;
    private EmotionType emotion;
    private ConcentrationLevel concentration;
    private BigDecimal confidenceScore;
    private String boundingBox;
    private String gazeDirection;
    private LocalDateTime capturedAt;
    private boolean anonymised;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSnapshotId() { return snapshotId; }
    public void setSnapshotId(String snapshotId) { this.snapshotId = snapshotId; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public EmotionType getEmotion() { return emotion; }
    public void setEmotion(EmotionType emotion) { this.emotion = emotion; }
    public ConcentrationLevel getConcentration() { return concentration; }
    public void setConcentration(ConcentrationLevel concentration) { this.concentration = concentration; }
    public BigDecimal getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(BigDecimal confidenceScore) { this.confidenceScore = confidenceScore; }
    public String getBoundingBox() { return boundingBox; }
    public void setBoundingBox(String boundingBox) { this.boundingBox = boundingBox; }
    public String getGazeDirection() { return gazeDirection; }
    public void setGazeDirection(String gazeDirection) { this.gazeDirection = gazeDirection; }
    public LocalDateTime getCapturedAt() { return capturedAt; }
    public void setCapturedAt(LocalDateTime capturedAt) { this.capturedAt = capturedAt; }
    public boolean isAnonymised() { return anonymised; }
    public void setAnonymised(boolean anonymised) { this.anonymised = anonymised; }
}
