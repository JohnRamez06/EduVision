// StudentSessionAttendanceDTO.java
package com.eduvision.dto.attendance;

import java.time.LocalDateTime;
import java.util.List;

public class StudentSessionAttendanceDTO {
    private boolean present;
    private String status;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
    private int totalExits;
    private List<ExitLogDTO> exits;

    public StudentSessionAttendanceDTO(boolean present, String status,
                                       LocalDateTime joinedAt, LocalDateTime leftAt,
                                       int totalExits, List<ExitLogDTO> exits) {
        this.present    = present;
        this.status     = status;
        this.joinedAt   = joinedAt;
        this.leftAt     = leftAt;
        this.totalExits = totalExits;
        this.exits      = exits;
    }

    public boolean isPresent()              { return present; }
    public String getStatus()               { return status; }
    public LocalDateTime getJoinedAt()      { return joinedAt; }
    public LocalDateTime getLeftAt()        { return leftAt; }
    public int getTotalExits()              { return totalExits; }
    public List<ExitLogDTO> getExits()      { return exits; }
}