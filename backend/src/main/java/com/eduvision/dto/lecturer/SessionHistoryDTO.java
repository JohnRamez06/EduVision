package com.eduvision.dto.lecturer;

import java.time.LocalDateTime;

public class SessionHistoryDTO {

    private String sessionId;
    private String courseName;
    private LocalDateTime date;
    private double avgEngagement;
    private int studentCount;
    private int alertCount;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public double getAvgEngagement() {
        return avgEngagement;
    }

    public void setAvgEngagement(double avgEngagement) {
        this.avgEngagement = avgEngagement;
    }

    public int getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(int studentCount) {
        this.studentCount = studentCount;
    }

    public int getAlertCount() {
        return alertCount;
    }

    public void setAlertCount(int alertCount) {
        this.alertCount = alertCount;
    }
}
