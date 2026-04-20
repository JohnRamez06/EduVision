package com.eduvision.observer;
public class StudentAtRiskEvent {
    private final String sessionId;
    private final String studentId;
    public StudentAtRiskEvent(String sessionId, String studentId) { this.sessionId=sessionId; this.studentId=studentId; }
    public String getSessionId() { return sessionId; }
    public String getStudentId() { return studentId; }
}
