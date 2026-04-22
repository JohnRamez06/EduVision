package com.eduvision.observer;

import java.math.BigDecimal;

public class SentimentAlertEvent {
    private final String sessionId;
    private final String snapshotId;
    private final BigDecimal engagementScore;
    private final BigDecimal concentration;

    public SentimentAlertEvent(String sessionId, double engagementScore, double concentration) {
        this(sessionId, null, BigDecimal.valueOf(engagementScore), BigDecimal.valueOf(concentration));
    }

    public SentimentAlertEvent(String sessionId, String snapshotId, BigDecimal engagementScore, BigDecimal concentration) {
        this.sessionId = sessionId;
        this.snapshotId = snapshotId;
        this.engagementScore = engagementScore;
        this.concentration = concentration;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getSnapshotId() {
        return snapshotId;
    }

    public BigDecimal getEngagementScore() {
        return engagementScore;
    }

    public BigDecimal getConcentration() {
        return concentration;
    }
}
