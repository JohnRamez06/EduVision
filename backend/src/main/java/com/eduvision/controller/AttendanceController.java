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

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
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

    @PostMapping("/exit")
    public ResponseEntity<?> recordExit(@RequestBody ExitRecordRequest request) {
        attendanceService.recordExit(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/return")
    public ResponseEntity<?> recordReturn(@RequestBody ReturnRecordRequest request) {
        attendanceService.recordReturn(request);
        return ResponseEntity.ok().build();
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

    @PostMapping("/record-with-presence")
    public ResponseEntity<?> recordAttendanceWithPresence(@RequestBody Map<String, Object> request) {
        String sessionId = asString(request.get("sessionId"));
        String studentId = asString(request.get("studentId"));
        String event = firstNonBlank(
                asString(request.get("event")),
                asString(request.get("presenceEvent")),
                asString(request.get("status")),
                "present"
        );

        if (sessionId == null || studentId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "sessionId and studentId are required"));
        }

        try {
            LocalDateTime joinedWhen = parseDateTime(firstPresent(
                    request, "joinedWhen", "joinedAt", "joined_at", "joined_when", "joinedTime"));
            LocalDateTime leftWhen = parseDateTime(firstPresent(
                    request, "leftWhen", "leftAt", "left_at", "left_when", "leftTime"));

            attendanceService.recordAttendanceWithPresence(sessionId, studentId, event, joinedWhen, leftWhen);
            return ResponseEntity.ok(Map.of("message", "Presence recorded successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error recording presence attendance: {}", e.getMessage());
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

    private static Object firstPresent(Map<String, Object> request, String... keys) {
        for (String key : keys) {
            if (request.containsKey(key) && request.get(key) != null) {
                return request.get(key);
            }
        }
        return null;
    }

    private static String asString(Object value) {
        if (value == null) return null;
        String s = String.valueOf(value).trim();
        return s.isEmpty() ? null : s;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) return value;
        }
        return null;
    }

    private static LocalDateTime parseDateTime(Object value) {
        String text = asString(value);
        if (text == null) return null;

        try {
            return LocalDateTime.parse(text);
        } catch (Exception ignored) {
        }
        try {
            return OffsetDateTime.parse(text).toLocalDateTime();
        } catch (Exception ignored) {
        }
        try {
            return LocalDateTime.parse(text.replace(" ", "T"));
        } catch (Exception ignored) {
        }

        throw new IllegalArgumentException("Invalid datetime format: " + text);
    }
}
