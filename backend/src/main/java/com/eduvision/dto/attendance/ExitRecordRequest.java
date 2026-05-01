// ExitRecordRequest.java
package com.eduvision.dto.attendance;

public class ExitRecordRequest {
    private String studentId;
    private String sessionId;
    private String exitType;

    public ExitRecordRequest() {}
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getExitType() { return exitType; }
    public void setExitType(String exitType) { this.exitType = exitType; }
}