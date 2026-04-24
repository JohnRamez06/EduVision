package com.eduvision.dto.alert;

public class AlertContext {
    private String sessionId;
    private String studentId;
    private double engagementScore;
    private double concentrationLevel;
    private String emotionType;
    private long timestamp;
    private double previousEngagementScore; // for trend
    private double trendSlope; // negative for downward trend

    // Constructors, getters, setters
    public AlertContext() {}

    public AlertContext(String sessionId, String studentId, double engagementScore, double concentrationLevel, String emotionType, long timestamp) {
        this.sessionId = sessionId;
        this.studentId = studentId;
        this.engagementScore = engagementScore;
        this.concentrationLevel = concentrationLevel;
        this.emotionType = emotionType;
        this.timestamp = timestamp;
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public double getEngagementScore() { return engagementScore; }
    public void setEngagementScore(double engagementScore) { this.engagementScore = engagementScore; }

    public double getConcentrationLevel() { return concentrationLevel; }
    public void setConcentrationLevel(double concentrationLevel) { this.concentrationLevel = concentrationLevel; }

    public String getEmotionType() { return emotionType; }
    public void setEmotionType(String emotionType) { this.emotionType = emotionType; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public double getPreviousEngagementScore() { return previousEngagementScore; }
    public void setPreviousEngagementScore(double previousEngagementScore) { this.previousEngagementScore = previousEngagementScore; }

    public double getTrendSlope() { return trendSlope; }
    public void setTrendSlope(double trendSlope) { this.trendSlope = trendSlope; }
}