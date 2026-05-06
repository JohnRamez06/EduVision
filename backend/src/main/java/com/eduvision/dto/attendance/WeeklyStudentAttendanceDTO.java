// src/main/java/com/eduvision/dto/attendance/WeeklyStudentAttendanceDTO.java
package com.eduvision.dto.attendance;

public class WeeklyStudentAttendanceDTO {
    private String studentId;
    private String studentNumber;
    private String studentName;
    private String autoStatus;
    private String manualStatus;
    private String finalStatus;
    private String notes;
    private Integer sessionsAttended;
    private Integer totalSessions;
    private Double attendanceRate;
    private Boolean isManuallyModified;
    private String courseName;

    // Constructors
    public WeeklyStudentAttendanceDTO() {}

    public WeeklyStudentAttendanceDTO(String studentId, String studentNumber, String studentName,
                                      String autoStatus, String manualStatus, String finalStatus,
                                      String notes, Integer sessionsAttended, Integer totalSessions,
                                      Double attendanceRate, Boolean isManuallyModified, String courseName) {
        this.studentId = studentId;
        this.studentNumber = studentNumber;
        this.studentName = studentName;
        this.autoStatus = autoStatus;
        this.manualStatus = manualStatus;
        this.finalStatus = finalStatus;
        this.notes = notes;
        this.sessionsAttended = sessionsAttended;
        this.totalSessions = totalSessions;
        this.attendanceRate = attendanceRate;
        this.isManuallyModified = isManuallyModified;
        this.courseName = courseName;
    }

    // Getters
    public String getStudentId() { return studentId; }
    public String getStudentNumber() { return studentNumber; }
    public String getStudentName() { return studentName; }
    public String getAutoStatus() { return autoStatus; }
    public String getManualStatus() { return manualStatus; }
    public String getFinalStatus() { return finalStatus; }
    public String getNotes() { return notes; }
    public Integer getSessionsAttended() { return sessionsAttended; }
    public Integer getTotalSessions() { return totalSessions; }
    public Double getAttendanceRate() { return attendanceRate; }
    public Boolean getIsManuallyModified() { return isManuallyModified; }
    public String getCourseName() { return courseName; }

    // Setters
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public void setAutoStatus(String autoStatus) { this.autoStatus = autoStatus; }
    public void setManualStatus(String manualStatus) { this.manualStatus = manualStatus; }
    public void setFinalStatus(String finalStatus) { this.finalStatus = finalStatus; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setSessionsAttended(Integer sessionsAttended) { this.sessionsAttended = sessionsAttended; }
    public void setTotalSessions(Integer totalSessions) { this.totalSessions = totalSessions; }
    public void setAttendanceRate(Double attendanceRate) { this.attendanceRate = attendanceRate; }
    public void setIsManuallyModified(Boolean isManuallyModified) { this.isManuallyModified = isManuallyModified; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
}