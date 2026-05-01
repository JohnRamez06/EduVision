// ExitLogDTO.java
package com.eduvision.dto.attendance;

import java.time.LocalDateTime;

public class ExitLogDTO {
    private LocalDateTime exitTime;
    private LocalDateTime returnTime;
    private Long durationMinutes;   // null if student never returned
    private String exitType;
    private String studentName;     // null when used in student-scoped queries

    public ExitLogDTO(LocalDateTime exitTime, LocalDateTime returnTime,
                      Long durationMinutes, String exitType, String studentName) {
        this.exitTime        = exitTime;
        this.returnTime      = returnTime;
        this.durationMinutes = durationMinutes;
        this.exitType        = exitType;
        this.studentName     = studentName;
    }

    public LocalDateTime getExitTime()        { return exitTime; }
    public LocalDateTime getReturnTime()      { return returnTime; }
    public Long getDurationMinutes()          { return durationMinutes; }
    public String getExitType()               { return exitType; }
    public String getStudentName()            { return studentName; }
}