package com.eduvision.observer;

import java.math.BigDecimal;

public class ConcentrationDropEvent {
    private final String sessionId;
    private final String snapshotId;
    private final String studentId;
    private final BigDecimal concentration;

    public ConcentrationDropEvent(String sessionId, String studentId) {
        this(sessionId, null, studentId, BigDecimal.ZERO);
    }

    public ConcentrationDropEvent(String sessionId, String snapshotId, String studentId, BigDecimal concentration) {
        this.sessionId = sessionId;
        this.snapshotId = snapshotId;
        this.studentId = studentId;
        this.concentration = concentration;
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

    public BigDecimal getConcentration() {
        return concentration;
    }
}
