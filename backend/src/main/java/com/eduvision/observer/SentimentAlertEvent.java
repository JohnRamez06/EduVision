package com.eduvision.observer;

import com.eduvision.model.AlertSeverity;

public class SentimentAlertEvent {

    private final String sessionId;
    private final String snapshotId;
    private final double engagementScore;
    private final double concentration;
    private final AlertSeverity severity;
    private final String message;

    public SentimentAlertEvent(String sessionId, String snapshotId, double engagementScore,
                               double concentration, AlertSeverity severity, String message) {
        this.sessionId = sessionId;
        this.snapshotId = snapshotId;
        this.engagementScore = engagementScore;
        this.concentration = concentration;
        this.severity = severity;
        this.message = message;
    }

    public String getSessionId() { return sessionId; }
    public String getSnapshotId() { return snapshotId; }
    public double getEngagementScore() { return engagementScore; }
    public double getConcentration() { return concentration; }
    public AlertSeverity getSeverity() { return severity; }
    public String getMessage() { return message; }
}
