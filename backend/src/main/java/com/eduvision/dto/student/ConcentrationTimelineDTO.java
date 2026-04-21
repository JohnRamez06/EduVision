package com.eduvision.dto.student;

import java.util.List;

public class ConcentrationTimelineDTO {

    private String       sessionId;
    private String       sessionTitle;
    private String       courseName;
    private List<String> timestamps;           // capturedAt as ISO-8601 strings
    private List<Double> concentrationScores;  // 0.0 (distracted) → 1.0 (high)
    private List<String> emotions;             // EmotionType.name() per snapshot

    // Getters & Setters
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getSessionTitle() { return sessionTitle; }
    public void setSessionTitle(String sessionTitle) { this.sessionTitle = sessionTitle; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public List<String> getTimestamps() { return timestamps; }
    public void setTimestamps(List<String> timestamps) { this.timestamps = timestamps; }
    public List<Double> getConcentrationScores() { return concentrationScores; }
    public void setConcentrationScores(List<Double> concentrationScores) { this.concentrationScores = concentrationScores; }
    public List<String> getEmotions() { return emotions; }
    public void setEmotions(List<String> emotions) { this.emotions = emotions; }
}