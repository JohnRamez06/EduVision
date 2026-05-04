// src/main/java/com/eduvision/dto/alert/AlertContext.java
package com.eduvision.dto.alert;

public class AlertContext {
    private String sessionId;
    private String courseId;
    private String courseName;
    private int studentCount;
    private double confusionRatio;
    private double engagementScore;
    private double avgConcentration;
    private String dominantEmotion;
    private java.time.LocalDateTime timestamp;

    public AlertContext() {}

    public AlertContext(String sessionId, String courseId, String courseName, 
                        int studentCount, double confusionRatio, double engagementScore,
                        double avgConcentration, String dominantEmotion) {
        this.sessionId = sessionId;
        this.courseId = courseId;
        this.courseName = courseName;
        this.studentCount = studentCount;
        this.confusionRatio = confusionRatio;
        this.engagementScore = engagementScore;
        this.avgConcentration = avgConcentration;
        this.dominantEmotion = dominantEmotion;
        this.timestamp = java.time.LocalDateTime.now();
    }

    // Getters
    public String getSessionId() { return sessionId; }
    public String getCourseId() { return courseId; }
    public String getCourseName() { return courseName; }
    public int getStudentCount() { return studentCount; }
    public double getConfusionRatio() { return confusionRatio; }
    public double getEngagementScore() { return engagementScore; }
    public double getAvgConcentration() { return avgConcentration; }
    public String getDominantEmotion() { return dominantEmotion; }
    public java.time.LocalDateTime getTimestamp() { return timestamp; }

    // Setters
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public void setStudentCount(int studentCount) { this.studentCount = studentCount; }
    public void setConfusionRatio(double confusionRatio) { this.confusionRatio = confusionRatio; }
    public void setEngagementScore(double engagementScore) { this.engagementScore = engagementScore; }
    public void setAvgConcentration(double avgConcentration) { this.avgConcentration = avgConcentration; }
    public void setDominantEmotion(String dominantEmotion) { this.dominantEmotion = dominantEmotion; }
    public void setTimestamp(java.time.LocalDateTime timestamp) { this.timestamp = timestamp; }
}