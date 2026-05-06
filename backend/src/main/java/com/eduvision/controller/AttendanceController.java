// src/main/java/com/eduvision/controller/AttendanceController.java
package com.eduvision.controller;

import com.eduvision.dto.attendance.ExitRecordRequest;
import com.eduvision.dto.attendance.ReturnRecordRequest;
import com.eduvision.dto.attendance.ExitLogDTO;
import com.eduvision.dto.attendance.StudentSessionAttendanceDTO;
import com.eduvision.dto.attendance.WeeklyAttendanceDTO;
import com.eduvision.service.AttendanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceController {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceController.class);
    private final AttendanceService attendanceService;
    private final JdbcTemplate jdbc;

    public AttendanceController(AttendanceService attendanceService, JdbcTemplate jdbc) {
        this.attendanceService = attendanceService;
        this.jdbc = jdbc;
    }

    /** Record a student exit from the session room. */
    @PostMapping("/exit")
    public ResponseEntity<?> recordExit(@RequestBody ExitRecordRequest request) {
        attendanceService.recordExit(request);
        return ResponseEntity.ok().build();
    }

    /** Record a student's return after an exit. */
    @PostMapping("/return")
    public ResponseEntity<?> recordReturn(@RequestBody ReturnRecordRequest request) {
        attendanceService.recordReturn(request);
        return ResponseEntity.ok().build();
    }

    /** All exit records for a session (for Lecturer view). */
    @GetMapping("/session/{sessionId}/exits")
    public ResponseEntity<List<ExitLogDTO>> getSessionExits(@PathVariable String sessionId) {
        return ResponseEntity.ok(attendanceService.getSessionExits(sessionId));
    }

    /** Full attendance detail for one student in one session. */
    @GetMapping("/student/{studentId}/session/{sessionId}")
    public ResponseEntity<StudentSessionAttendanceDTO> getStudentSessionAttendance(
            @PathVariable String studentId,
            @PathVariable String sessionId) {
        return ResponseEntity.ok(
                attendanceService.getStudentSessionAttendance(studentId, sessionId));
    }

    /** Weekly attendance for the authenticated student. */
    @GetMapping("/weekly/{weekId}")
    public ResponseEntity<List<WeeklyAttendanceDTO>> getWeeklyAttendance(
            @PathVariable String weekId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                attendanceService.getWeeklyAttendance(userDetails.getUsername(), weekId));
    }

    /** Weekly attendance for all students in a course (Lecturer / Dean view). */
    @GetMapping("/course/{courseId}/weekly/{weekId}")
    public ResponseEntity<List<WeeklyAttendanceDTO>> getCourseWeeklyAttendance(
            @PathVariable String courseId,
            @PathVariable String weekId) {
        return ResponseEntity.ok(
                attendanceService.getCourseWeeklyAttendance(courseId, weekId));
    }

    /** Trigger weekly attendance calculation (Admin/Scheduler only) */
    @PostMapping("/weekly/calculate")
    public ResponseEntity<?> triggerWeeklyAttendance() {
        attendanceService.calculateWeeklyAttendance();
        return ResponseEntity.ok(Map.of("message", "Weekly attendance calculated successfully"));
    }

    /** Record student attendance (called from Python) */
    @PostMapping("/record")
    public ResponseEntity<?> recordAttendance(@RequestBody Map<String, String> request) {
        String sessionId = request.get("sessionId");
        String studentId = request.get("studentId");
        String status = request.getOrDefault("status", "present");
        
        if (sessionId == null || studentId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "sessionId and studentId are required"));
        }
        
        try {
            attendanceService.recordAttendance(sessionId, studentId, status);
            return ResponseEntity.ok(Map.of("message", "Attendance recorded successfully"));
        } catch (Exception e) {
            logger.error("Error recording attendance: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /** Check attendance for a session */
    @GetMapping("/check/{sessionId}")
    public ResponseEntity<?> checkAttendance(@PathVariable String sessionId) {
        Map<String, Object> result = attendanceService.getSessionAttendanceSummary(sessionId);
        return ResponseEntity.ok(result);
    }

    /** Check if student is enrolled in session (for Python) */
    @GetMapping("/session/{sessionId}/check-student/{studentId}")
    public ResponseEntity<?> checkStudentEnrollment(
            @PathVariable String sessionId,
            @PathVariable String studentId) {
        
        logger.info("🔍 Enrollment check: session={}, student={}", sessionId, studentId);
        
        boolean enrolled = attendanceService.isStudentEnrolledInSession(sessionId, studentId);
        
        logger.info("📊 Enrollment result: {}", enrolled);
        
        return ResponseEntity.ok(Map.of("enrolled", enrolled));
    }

    /** Get available weeks for manual attendance */
    @GetMapping("/manual/weeks")
    public ResponseEntity<List<Map<String, Object>>> getAvailableWeeks(@RequestParam String courseId) {
        List<Map<String, Object>> weeks = attendanceService.getAvailableWeeksForCourse(courseId);
        return ResponseEntity.ok(weeks);
    }

    /** Get students for manual attendance */
    @GetMapping("/manual/students")
    public ResponseEntity<List<Map<String, Object>>> getStudentsForManualAttendance(
            @RequestParam String courseId,
            @RequestParam String weekId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String lecturerId = getLecturerIdFromEmail(userDetails.getUsername());
        List<Map<String, Object>> students = attendanceService.getStudentsForManualAttendance(courseId, weekId, lecturerId);
        return ResponseEntity.ok(students);
    }

    /** Save manual attendance */
    @PostMapping("/manual/save")
public ResponseEntity<?> saveManualAttendance(
        @RequestBody Map<String, Object> request,
        @AuthenticationPrincipal UserDetails userDetails) {
    
    logger.info("Received manual attendance request: {}", request);
    
    String courseId = (String) request.get("courseId");
    String weekId = (String) request.get("weekId");
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> students = (List<Map<String, Object>>) request.get("students");
    
    logger.info("Course ID: {}, Week ID: {}, Students count: {}", courseId, weekId, students.size());
    
    String lecturerId = getLecturerIdFromEmail(userDetails.getUsername());
    
    attendanceService.saveManualAttendance(courseId, weekId, students, lecturerId);
    return ResponseEntity.ok(Map.of("message", "Manual attendance saved successfully"));
}

    // Helper method to get lecturer ID from email
    private String getLecturerIdFromEmail(String email) {
        try {
            return jdbc.queryForObject(
                "SELECT l.user_id FROM lecturers l JOIN users u ON u.id = l.user_id WHERE u.email = ?",
                String.class, email);
        } catch (Exception e) {
            logger.error("Error getting lecturer ID from email {}: {}", email, e.getMessage());
            return null;
        }
    }
}