package com.eduvision.observer;

import java.math.BigDecimal;

public class StudentAtRiskEvent {
    private final String sessionId;
    private final String snapshotId;
    private final String studentId;
    private final BigDecimal confidenceScore;

    public StudentAtRiskEvent(String sessionId, String studentId) {
        this(sessionId, null, studentId, BigDecimal.ZERO);
    }

    public StudentAtRiskEvent(String sessionId, String snapshotId, String studentId, BigDecimal confidenceScore) {
        this.sessionId = sessionId;
        this.snapshotId = snapshotId;
        this.studentId = studentId;
        this.confidenceScore = confidenceScore;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getSnapshotId() {
        return snapshotId;
    }

    public String getStudentId() {
        return studentId;
    }

    public BigDecimal getConfidenceScore() {
        return confidenceScore;
    }
}
