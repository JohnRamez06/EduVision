package com.eduvision.pattern.observer;
public class SentimentAlertEvent {
    private final String sessionId;
    private final double engagementScore;
    public SentimentAlertEvent(String sessionId, double engagementScore) {
        this.sessionId = sessionId; this.engagementScore = engagementScore;
    }
    public String getSessionId() { return sessionId; }
    public double getEngagementScore() { return engagementScore; }
}
