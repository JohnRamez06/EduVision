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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceController {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceController.class);
    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
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
}