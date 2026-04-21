package com.eduvision.dto.websocket;

import java.time.LocalDateTime;

public class MoodUpdateDTO {

    private String        sessionId;
    private String        dominantEmotion;    // EmotionType.name(): happy/sad/angry/confused/neutral/engaged
    private double        engagementScore;    // 0.0 – 1.0  (from EmotionSnapshot.engagementScore)
    private double        concentration;      // 0.0 – 1.0  (mapped from ConcentrationLevel)
    private int           studentCount;       // how many students were detected in this snapshot
    private LocalDateTime timestamp;          // snapshot capturedAt

    public MoodUpdateDTO() {}

    public MoodUpdateDTO(String sessionId, String dominantEmotion,
                          double engagementScore, double concentration,
                          int studentCount) {
        this.sessionId      = sessionId;
        this.dominantEmotion = dominantEmotion;
        this.engagementScore = engagementScore;
        this.concentration  = concentration;
        this.studentCount   = studentCount;
        this.timestamp      = LocalDateTime.now();
    }

    // Getters & Setters
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getDominantEmotion() { return dominantEmotion; }
    public void setDominantEmotion(String dominantEmotion) { this.dominantEmotion = dominantEmotion; }

    public double getEngagementScore() { return engagementScore; }
    public void setEngagementScore(double engagementScore) { this.engagementScore = engagementScore; }

    public double getConcentration() { return concentration; }
    public void setConcentration(double concentration) { this.concentration = concentration; }

    public int getStudentCount() { return studentCount; }
    public void setStudentCount(int studentCount) { this.studentCount = studentCount; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}