// ReturnRecordRequest.java
package com.eduvision.dto.attendance;

public class ReturnRecordRequest {
    private String studentId;
    private String sessionId;

    public ReturnRecordRequest() {}
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
}