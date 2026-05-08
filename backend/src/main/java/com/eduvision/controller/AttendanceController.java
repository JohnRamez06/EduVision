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

    @GetMapping("/session/{sessionId}/exits")
    public ResponseEntity<List<ExitLogDTO>> getSessionExits(@PathVariable String sessionId) {
        return ResponseEntity.ok(attendanceService.getSessionExits(sessionId));
    }

    @GetMapping("/student/{studentId}/session/{sessionId}")
    public ResponseEntity<StudentSessionAttendanceDTO> getStudentSessionAttendance(
            @PathVariable String studentId,
            @PathVariable String sessionId) {
        return ResponseEntity.ok(
                attendanceService.getStudentSessionAttendance(studentId, sessionId));
    }

    @GetMapping("/weekly/{weekId}")
    public ResponseEntity<List<WeeklyAttendanceDTO>> getWeeklyAttendance(
            @PathVariable String weekId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                attendanceService.getWeeklyAttendance(userDetails.getUsername(), weekId));
    }

    @GetMapping("/course/{courseId}/weekly/{weekId}")
    public ResponseEntity<List<WeeklyAttendanceDTO>> getCourseWeeklyAttendance(
            @PathVariable String courseId,
            @PathVariable String weekId) {
        return ResponseEntity.ok(
                attendanceService.getCourseWeeklyAttendance(courseId, weekId));
    }

    @PostMapping("/weekly/calculate")
    public ResponseEntity<?> triggerWeeklyAttendance() {
        attendanceService.calculateWeeklyAttendance();
        return ResponseEntity.ok(Map.of("message", "Weekly attendance calculated successfully"));
    }

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

    @GetMapping("/check/{sessionId}")
    public ResponseEntity<?> checkAttendance(@PathVariable String sessionId) {
        Map<String, Object> result = attendanceService.getSessionAttendanceSummary(sessionId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/session/{sessionId}/check-student/{studentId}")
    public ResponseEntity<?> checkStudentEnrollment(
            @PathVariable String sessionId,
            @PathVariable String studentId) {
        
        logger.info("🔍 Enrollment check: session={}, student={}", sessionId, studentId);
        boolean enrolled = attendanceService.isStudentEnrolledInSession(sessionId, studentId);
        logger.info("📊 Enrollment result: {}", enrolled);
        return ResponseEntity.ok(Map.of("enrolled", enrolled));
    }

    @GetMapping("/manual/weeks")
    public ResponseEntity<List<Map<String, Object>>> getAvailableWeeks(@RequestParam String courseId) {
        List<Map<String, Object>> weeks = attendanceService.getAvailableWeeksForCourse(courseId);
        return ResponseEntity.ok(weeks);
    }

    @GetMapping("/manual/students")
    public ResponseEntity<List<Map<String, Object>>> getStudentsForManualAttendance(
            @RequestParam String courseId,
            @RequestParam String weekId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String lecturerId = getLecturerIdFromEmail(userDetails.getUsername());
        List<Map<String, Object>> students = attendanceService.getStudentsForManualAttendance(courseId, weekId, lecturerId);
        return ResponseEntity.ok(students);
    }

    @PostMapping("/manual/save")
    public ResponseEntity<?> saveManualAttendance(@RequestBody Map<String, Object> request) {
        try {
            logger.info("=== RECEIVED MANUAL ATTENDANCE REQUEST ===");
            logger.info("Request body: {}", request);
            
            String courseId = (String) request.get("courseId");
            String weekId = (String) request.get("weekId");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> students = (List<Map<String, Object>>) request.get("students");
            String lecturerId = getLecturerIdFromEmail("lecturer@gmail.com");
            
            attendanceService.saveManualAttendance(courseId, weekId, students, lecturerId);
            
            logger.info("=== SAVE COMPLETED SUCCESSFULLY ===");
            return ResponseEntity.ok(Map.of("message", "Manual attendance saved successfully"));
            
        } catch (Exception e) {
            logger.error("Error saving manual attendance: ", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    private String getLecturerIdFromEmail(String email) {
        try {
            return jdbc.queryForObject(
                "SELECT l.user_id FROM lecturers l JOIN users u ON u.id = l.user_id WHERE u.email = ?",
                String.class, email);
        } catch (Exception e) {
            logger.error("Error getting lecturer ID from email {}: {}", email, e.getMessage());
            return "19b98b4d-b8fd-4af7-9328-a7a801cda18c"; // Fallback to lecturer@gmail.com
        }
    }

    /**
 * Get presence summary for a session (join/leave times)
 */
@GetMapping("/session/{sessionId}/presence-summary")
public ResponseEntity<List<Map<String, Object>>> getPresenceSummary(@PathVariable String sessionId) {
    return ResponseEntity.ok(attendanceService.getPresenceSummary(sessionId));
}

/**
 * Manually record student exit (e.g., student leaves to bathroom)
 */
@PostMapping("/session/{sessionId}/manual-exit")
public ResponseEntity<?> manualExit(
        @PathVariable String sessionId,
        @RequestBody Map<String, String> request) {
    
    String studentId = request.get("studentId");
    String reason = request.getOrDefault("reason", "manual");
    
    attendanceService.recordExit(sessionId, studentId, reason);
    return ResponseEntity.ok(Map.of("message", "Exit recorded"));
}

/**
 * Manually record student return
 */
@PostMapping("/session/{sessionId}/manual-return")
public ResponseEntity<?> manualReturn(
        @PathVariable String sessionId,
        @RequestBody Map<String, String> request) {
    
    String studentId = request.get("studentId");
    
    attendanceService.recordReturn(sessionId, studentId);
    return ResponseEntity.ok(Map.of("message", "Return recorded"));
}

@PostMapping("/record-with-presence")
public ResponseEntity<?> recordAttendanceWithPresence(@RequestBody Map<String, Object> request) {
    String sessionId = (String) request.get("sessionId");
    String studentId = (String) request.get("studentId");
    String presenceEvent = (String) request.get("presenceEvent");

    logger.info("📥 Presence event: {} for student {} in session {}", presenceEvent, studentId, sessionId);

    try {
        if ("joined".equals(presenceEvent)) {
            String sql = """
                INSERT INTO session_attendance (id, session_id, student_id, status, joined_at, recorded_at)
                VALUES (UUID(), ?, ?, 'present', NOW(), NOW())
                ON DUPLICATE KEY UPDATE
                    joined_at = COALESCE(joined_at, NOW()),
                    recorded_at = NOW()
            """;
            jdbc.update(sql, sessionId, studentId);
            logger.info("✅ JOIN recorded");
        }
        else if ("returned".equals(presenceEvent)) {
            String sql = """
                UPDATE session_attendance
                SET left_at = NULL, recorded_at = NOW()
                WHERE session_id = ? AND student_id = ?
            """;
            jdbc.update(sql, sessionId, studentId);
            logger.info("🔄 RETURN recorded");
        }
        else if ("left".equals(presenceEvent)) {
            // 1. Update session_attendance – set left_at if it's currently NULL
            String updateSql = """
                UPDATE session_attendance
                SET left_at = NOW()
                WHERE session_id = ? AND student_id = ? AND left_at IS NULL
            """;
            int updated = jdbc.update(updateSql, sessionId, studentId);

            // 2. Insert exit log (only if the update succeeded)
            if (updated > 0) {
                String exitSql = """
                    INSERT INTO session_exit_logs (id, session_id, student_id, exit_time, exit_type, detected_by)
                    VALUES (UUID(), ?, ?, NOW(), 'camera_loss', 'camera')
                """;
                jdbc.update(exitSql, sessionId, studentId);
                logger.info("🚪 LEFT recorded");
            } else {
                logger.warn("No row updated for left event – student may not have been joined or already left");
            }
        }
        else {
            // regular presence – ensure a row exists
            String sql = """
                INSERT INTO session_attendance (id, session_id, student_id, status, recorded_at)
                VALUES (UUID(), ?, ?, 'present', NOW())
                ON DUPLICATE KEY UPDATE recorded_at = NOW()
            """;
            jdbc.update(sql, sessionId, studentId);
        }
        return ResponseEntity.ok(Map.of("message", "Attendance with presence recorded"));
    }
    catch (Exception e) {
        logger.error("Error recording presence: {}", e.getMessage(), e);
        return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
    }
}
}