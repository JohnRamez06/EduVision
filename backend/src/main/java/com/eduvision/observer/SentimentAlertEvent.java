package com.eduvision.observer;
public class SentimentAlertEvent {
    private final String sessionId;
    private final double engagementScore;
    private final double concentration;
    public SentimentAlertEvent(String sessionId, double engagementScore, double concentration) {
        this.sessionId=sessionId; this.engagementScore=engagementScore; this.concentration=concentration;
    }
    public String getSessionId() { return sessionId; }
    public double getEngagementScore() { return engagementScore; }
    public double getConcentration() { return concentration; }
}
