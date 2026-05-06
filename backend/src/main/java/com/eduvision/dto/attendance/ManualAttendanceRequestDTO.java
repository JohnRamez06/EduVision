package com.eduvision.dto.attendance;

import lombok.Data;
import java.util.List;

@Data
public class ManualAttendanceRequestDTO {
    private String courseId;
    private String weekId;
    private List<StudentAttendance> students;
    
    @Data
    public static class StudentAttendance {
        private String studentId;
        private String status; // present, absent, excused
        private String notes;
    }

}

