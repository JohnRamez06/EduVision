// WeeklyAttendanceDTO.java
package com.eduvision.dto.attendance;

public class WeeklyAttendanceDTO {
    private String courseId;
    private String courseName;
    private int sessionsHeld;
    private int sessionsAttended;
    private int sessionsMissed;
    private int totalExits;
    private double attendanceRate;
    private String status;          // "regular" | "at_risk" | "absent"

    public WeeklyAttendanceDTO(String courseId, String courseName,
                               int sessionsHeld, int sessionsAttended,
                               int sessionsMissed, int totalExits,
                               double attendanceRate, String status) {
        this.courseId         = courseId;
        this.courseName       = courseName;
        this.sessionsHeld     = sessionsHeld;
        this.sessionsAttended = sessionsAttended;
        this.sessionsMissed   = sessionsMissed;
        this.totalExits       = totalExits;
        this.attendanceRate   = attendanceRate;
        this.status           = status;
    }

    public String getCourseId()         { return courseId; }
    public String getCourseName()       { return courseName; }
    public int getSessionsHeld()        { return sessionsHeld; }
    public int getSessionsAttended()    { return sessionsAttended; }
    public int getSessionsMissed()      { return sessionsMissed; }
    public int getTotalExits()          { return totalExits; }
    public double getAttendanceRate()   { return attendanceRate; }
    public String getStatus()           { return status; }
}