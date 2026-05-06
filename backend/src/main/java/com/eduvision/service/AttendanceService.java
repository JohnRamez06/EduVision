// src/main/java/com/eduvision/service/AttendanceService.java
package com.eduvision.service;

import com.eduvision.dto.attendance.ExitRecordRequest;
import com.eduvision.dto.attendance.ManualAttendanceRequestDTO;
import com.eduvision.dto.attendance.ExitLogDTO;
import com.eduvision.dto.attendance.ReturnRecordRequest;
import com.eduvision.dto.attendance.StudentSessionAttendanceDTO;
import com.eduvision.dto.attendance.WeeklyAttendanceDTO;
import com.eduvision.dto.attendance.WeeklyStudentAttendanceDTO;
import com.eduvision.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AttendanceService {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceService.class);
    private final JdbcTemplate jdbc;

    public AttendanceService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }
    // ============================================================
    // SESSION ATTENDANCE SUMMARY
    // ============================================================

    public Map<String, Object> getSessionAttendanceSummary(String sessionId) {
        String countSql = "SELECT COUNT(*) FROM session_attendance WHERE session_id = ?";
        Integer count = jdbc.queryForObject(countSql, Integer.class, sessionId);
        
        String detailSql = """
            SELECT sa.student_id, sa.status, sa.recorded_at
            FROM session_attendance sa
            WHERE sa.session_id = ?
            ORDER BY sa.recorded_at DESC
            LIMIT 20
        """;
        
        List<Map<String, Object>> records = jdbc.queryForList(detailSql, sessionId);
        
        return Map.of(
            "sessionId", sessionId,
            "totalRecords", count,
            "records", records
        );
    }

    // ============================================================
    // EXIT / RETURN LOGS
    // ============================================================

    @Transactional
    public void recordExit(ExitRecordRequest req) {
        Integer studentExists = jdbc.queryForObject(
                "SELECT COUNT(*) FROM students WHERE student_id = ?",
                Integer.class, req.getStudentId());
        if (studentExists == null || studentExists == 0)
            throw new ResourceNotFoundException("Student not found: " + req.getStudentId());

        Integer sessionExists = jdbc.queryForObject(
                "SELECT COUNT(*) FROM lecture_sessions WHERE session_id = ?",
                Integer.class, req.getSessionId());
        if (sessionExists == null || sessionExists == 0)
            throw new ResourceNotFoundException("Session not found: " + req.getSessionId());

        String id = UUID.randomUUID().toString();
        jdbc.update(
                "INSERT INTO session_exit_logs (exit_log_id, student_id, session_id, exit_time, exit_type) " +
                "VALUES (?, ?, ?, ?, ?)",
                id, req.getStudentId(), req.getSessionId(),
                LocalDateTime.now(), req.getExitType());
    }

    @Transactional
    public void recordReturn(ReturnRecordRequest req) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT exit_log_id FROM session_exit_logs " +
                "WHERE student_id = ? AND session_id = ? AND return_time IS NULL " +
                "ORDER BY exit_time DESC LIMIT 1",
                req.getStudentId(), req.getSessionId());

        if (rows.isEmpty())
            throw new ResourceNotFoundException("No open exit found for student " + req.getStudentId());

        String exitLogId = (String) rows.get(0).get("exit_log_id");
        jdbc.update(
                "UPDATE session_exit_logs SET return_time = ? WHERE exit_log_id = ?",
                LocalDateTime.now(), exitLogId);
    }

    public List<ExitLogDTO> getSessionExits(String sessionId) {
        return jdbc.query(
                "SELECT el.exit_time, el.return_time, el.exit_type, " +
                "       CONCAT(u.first_name, ' ', u.last_name) AS student_name " +
                "FROM session_exit_logs el " +
                "JOIN students s ON s.student_id = el.student_id " +
                "JOIN users u ON u.user_id = s.user_id " +
                "WHERE el.session_id = ? " +
                "ORDER BY el.exit_time DESC",
                (rs, row) -> {
                    LocalDateTime exit = rs.getObject("exit_time", LocalDateTime.class);
                    LocalDateTime ret = rs.getObject("return_time", LocalDateTime.class);
                    long duration = (ret != null) ? ChronoUnit.MINUTES.between(exit, ret) : -1;
                    return new ExitLogDTO(
                            exit, ret,
                            duration >= 0 ? duration : null,
                            rs.getString("exit_type"),
                            rs.getString("student_name"));
                },
                sessionId);
    }

    // ============================================================
    // STUDENT SESSION ATTENDANCE
    // ============================================================

    public StudentSessionAttendanceDTO getStudentSessionAttendance(String studentId, String sessionId) {
        List<Map<String, Object>> attRows = jdbc.queryForList(
                "SELECT status, joined_at, left_at FROM session_attendance " +
                "WHERE student_id = ? AND session_id = ?",
                studentId, sessionId);

        boolean present = false;
        String status = "absent";
        LocalDateTime joinedAt = null, leftAt = null;

        if (!attRows.isEmpty()) {
            Map<String, Object> att = attRows.get(0);
            status = (String) att.get("status");
            present = !"absent".equalsIgnoreCase(status);
            joinedAt = (LocalDateTime) att.get("joined_at");
            leftAt = (LocalDateTime) att.get("left_at");
        }

        List<ExitLogDTO> exits = jdbc.query(
                "SELECT exit_time, return_time, exit_type FROM session_exit_logs " +
                "WHERE student_id = ? AND session_id = ? ORDER BY exit_time",
                (rs, row) -> {
                    LocalDateTime exit = rs.getObject("exit_time", LocalDateTime.class);
                    LocalDateTime ret = rs.getObject("return_time", LocalDateTime.class);
                    long mins = (ret != null) ? ChronoUnit.MINUTES.between(exit, ret) : -1;
                    return new ExitLogDTO(exit, ret, mins >= 0 ? mins : null,
                            rs.getString("exit_type"), null);
                },
                studentId, sessionId);

        return new StudentSessionAttendanceDTO(
                present, status, joinedAt, leftAt, exits.size(), exits);
    }

    // ============================================================
    // WEEKLY ATTENDANCE
    // ============================================================

    public List<WeeklyAttendanceDTO> getWeeklyAttendance(String email, String weekId) {
        String studentId = jdbc.queryForObject(
                "SELECT s.student_id FROM students s JOIN users u ON u.user_id = s.user_id " +
                "WHERE u.email = ?",
                String.class, email);

        return jdbc.query(
                "SELECT wca.course_id, c.course_name, " +
                "       wca.sessions_held, wca.sessions_attended, wca.sessions_missed, " +
                "       wca.total_exits, wca.attendance_rate, wca.status " +
                "FROM weekly_course_attendance wca " +
                "JOIN courses c ON c.course_id = wca.course_id " +
                "WHERE wca.student_id = ? AND wca.week_id = ?",
                (rs, row) -> new WeeklyAttendanceDTO(
                        rs.getString("course_id"),
                        rs.getString("course_name"),
                        rs.getInt("sessions_held"),
                        rs.getInt("sessions_attended"),
                        rs.getInt("sessions_missed"),
                        rs.getInt("total_exits"),
                        rs.getDouble("attendance_rate"),
                        rs.getString("status")),
                studentId, weekId);
    }

    public List<WeeklyAttendanceDTO> getCourseWeeklyAttendance(String courseId, String weekId) {
        return jdbc.query(
                "SELECT wca.course_id, c.course_name, " +
                "       wca.sessions_held, wca.sessions_attended, wca.sessions_missed, " +
                "       wca.total_exits, wca.attendance_rate, wca.status, " +
                "       CONCAT(u.first_name, ' ', u.last_name) AS student_name " +
                "FROM weekly_course_attendance wca " +
                "JOIN courses c ON c.course_id = wca.course_id " +
                "JOIN students s ON s.student_id = wca.student_id " +
                "JOIN users u ON u.user_id = s.user_id " +
                "WHERE wca.course_id = ? AND wca.week_id = ? " +
                "ORDER BY u.last_name, u.first_name",
                (rs, row) -> new WeeklyAttendanceDTO(
                        rs.getString("course_id"),
                        rs.getString("course_name"),
                        rs.getInt("sessions_held"),
                        rs.getInt("sessions_attended"),
                        rs.getInt("sessions_missed"),
                        rs.getInt("total_exits"),
                        rs.getDouble("attendance_rate"),
                        rs.getString("status")),
                courseId, weekId);
    }

    // ============================================================
    // WEEKLY ATTENDANCE CALCULATION
    // ============================================================

    public void calculateWeeklyAttendance() {
    logger.info("Starting weekly attendance calculation...");
    
    LocalDate today = LocalDate.now();
    LocalDate monday = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
    LocalDate sunday = monday.plusDays(6);
    
    int weekNumber = monday.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear());
    int year = monday.getYear();
    
    logger.info("Calculating for week {} of {}", weekNumber, year);
    
    String periodId = getOrCreateWeekPeriod(weekNumber, year, monday, sunday);
    
    String assignmentsSql = """
        SELECT cs.student_id, cs.course_id
        FROM course_students cs
        WHERE cs.dropped_at IS NULL
    """;
    
    List<Map<String, Object>> assignments = jdbc.queryForList(assignmentsSql);
    
    int present = 0, absent = 0;
    
    for (Map<String, Object> assignment : assignments) {
        String studentId = (String) assignment.get("student_id");
        String courseId = (String) assignment.get("course_id");
        
        // Check if student attended any session this week (including excused)
        String attendanceSql = """
            SELECT 
                CASE 
                    WHEN COUNT(DISTINCT sa.session_id) > 0 THEN 'present'
                    WHEN EXISTS (SELECT 1 FROM manual_attendance ma 
                                 WHERE ma.student_id = ? 
                                   AND ma.course_id = ? 
                                   AND ma.week_id = ? 
                                   AND ma.status = 'excused') THEN 'excused'
                    ELSE 'absent'
                END as status,
                COUNT(DISTINCT sa.session_id) as attended_count
            FROM lecture_sessions ls
            LEFT JOIN session_attendance sa ON sa.session_id = ls.id AND sa.student_id = ? AND sa.status = 'present'
            WHERE ls.course_id = ? 
              AND DATE(ls.scheduled_start) BETWEEN ? AND ?
              AND ls.status = 'completed'
        """;
        
        Map<String, Object> result = jdbc.queryForMap(attendanceSql, 
            studentId, courseId, periodId, studentId, courseId, monday, sunday);
        
        String status = (String) result.get("status");
        Number attendedCountNum = (Number) result.get("attended_count");
        int attendedCount = attendedCountNum != null ? attendedCountNum.intValue() : 0;
        
        // Check if any sessions were held this week
        String sessionsSql = """
            SELECT COUNT(*) as session_count
            FROM lecture_sessions
            WHERE course_id = ?
              AND DATE(scheduled_start) BETWEEN ? AND ?
              AND status = 'completed'
        """;
        
        Integer sessionCount = jdbc.queryForObject(sessionsSql, Integer.class, 
            courseId, monday, sunday);
        
        String finalStatus;
        if (sessionCount == null || sessionCount == 0) {
            finalStatus = "regular";
            present++;
        } else if (status.equals("present") || status.equals("excused")) {
            // 🔥 KEY CHANGE: Excused counts as present!
            finalStatus = "regular";
            present++;
        } else {
            finalStatus = "absent";
            absent++;
        }
        
        // Update weekly attendance
        updateWeeklyAttendance(periodId, studentId, courseId, 
            sessionCount != null ? sessionCount : 0, attendedCount, finalStatus);
    }
    
    logger.info("Weekly calculation complete: Present={}, Absent={}", present, absent);
}

    private String getOrCreateWeekPeriod(int weekNumber, int year, LocalDate startDate, LocalDate endDate) {
        String selectSql = "SELECT id FROM weekly_periods WHERE week_number = ? AND year = ?";
        List<String> ids = jdbc.queryForList(selectSql, String.class, weekNumber, year);
        
        if (!ids.isEmpty()) {
            return ids.get(0);
        }
        
        String id = UUID.randomUUID().toString();
        String insertSql = """
            INSERT INTO weekly_periods (id, week_number, year, start_date, end_date)
            VALUES (?, ?, ?, ?, ?)
        """;
        jdbc.update(insertSql, id, weekNumber, year, startDate, endDate);
        return id;
    }

    private void updateWeeklyAttendance(String periodId, String studentId, String courseId, 
                                         int sessionsHeld, int sessionsAttended, String status) {
        String selectSql = """
            SELECT id FROM weekly_course_attendance 
            WHERE week_id = ? AND student_id = ? AND course_id = ?
        """;
        
        List<String> ids = jdbc.queryForList(selectSql, String.class, periodId, studentId, courseId);
        
        if (!ids.isEmpty()) {
            String updateSql = """
                UPDATE weekly_course_attendance 
                SET sessions_held = ?, sessions_attended = ?, sessions_missed = ?,
                    attendance_rate = ?, status = ?, last_updated = NOW()
                WHERE id = ?
            """;
            int sessionsMissed = sessionsHeld - sessionsAttended;
            double attendanceRate = sessionsHeld > 0 ? (sessionsAttended * 100.0 / sessionsHeld) : 0;
            jdbc.update(updateSql, sessionsHeld, sessionsAttended, sessionsMissed, attendanceRate, status, ids.get(0));
        } else {
            String insertSql = """
                INSERT INTO weekly_course_attendance 
                (id, week_id, student_id, course_id, sessions_held, sessions_attended, 
                 sessions_missed, attendance_rate, status, last_updated)
                VALUES (UUID(), ?, ?, ?, ?, ?, ?, ?, ?, NOW())
            """;
            int sessionsMissed = sessionsHeld - sessionsAttended;
            double attendanceRate = sessionsHeld > 0 ? (sessionsAttended * 100.0 / sessionsHeld) : 0;
            jdbc.update(insertSql, periodId, studentId, courseId, sessionsHeld, sessionsAttended, 
                        sessionsMissed, attendanceRate, status);
        }
    }

// Update your recordAttendance method in AttendanceService.java

@Transactional
public void recordAttendance(String sessionId, String studentId, String status) {
    try {
        // First, verify student is enrolled in the course for this session
        boolean isEnrolled = isStudentEnrolledInSession(sessionId, studentId);
        
        if (!isEnrolled) {
            logger.warn("⚠️ Student {} not enrolled in session {} - skipping attendance", studentId, sessionId);
            return; // Skip - student not enrolled
        }
        
        // Check if attendance already exists
        String checkSql = "SELECT COUNT(*) FROM session_attendance WHERE session_id = ? AND student_id = ?";
        Integer count = jdbc.queryForObject(checkSql, Integer.class, sessionId, studentId);
        
        if (count == 0) {
            String insertSql = """
                INSERT INTO session_attendance (id, session_id, student_id, status, recorded_at)
                VALUES (UUID(), ?, ?, ?, NOW())
            """;
            jdbc.update(insertSql, sessionId, studentId, status);
            logger.info("✅ Attendance recorded: student={}, session={}", studentId, sessionId);
        } else {
            // Update existing record
            String updateSql = """
                UPDATE session_attendance 
                SET status = ?, recorded_at = NOW()
                WHERE session_id = ? AND student_id = ?
            """;
            jdbc.update(updateSql, status, sessionId, studentId);
            logger.info("🔄 Attendance updated: student={}, session={}", studentId, sessionId);
        }
    } catch (Exception e) {
        logger.error("Failed to record attendance: {}", e.getMessage());
        throw new RuntimeException("Failed to record attendance", e);
    }
}
// Add this method to AttendanceService.java

// src/main/java/com/eduvision/service/AttendanceService.java

// Add this method to AttendanceService class

// Use 'jdbc' instead of 'jdbcTemplate'
public boolean isStudentEnrolledInSession(String sessionId, String studentId) {
    try {
        String sql = """
            SELECT COUNT(*) FROM lecture_sessions ls
            JOIN course_students cs ON cs.course_id = ls.course_id
            WHERE ls.id = ? 
              AND cs.student_id = ? 
              AND cs.dropped_at IS NULL
              AND cs.lecturer_id = ls.lecturer_id
        """;
        
        // 🔥 Use 'jdbc' (not jdbcTemplate)
        Integer count = jdbc.queryForObject(sql, Integer.class, sessionId, studentId);
        boolean enrolled = count != null && count > 0;
        
        logger.info("Enrollment check: session={}, student={}, result={}", sessionId, studentId, enrolled);
        return enrolled;
    } catch (Exception e) {
        logger.error("Error checking enrollment: {}", e.getMessage());
        return false;
    }
}


// Add to AttendanceService.java
public String getLecturerIdByEmail(String email) {
    try {
        String sql = """
            SELECT l.user_id FROM lecturers l 
            JOIN users u ON u.id = l.user_id 
            WHERE u.email = ?
        """;
        return jdbc.queryForObject(sql, String.class, email);
    } catch (Exception e) {
        logger.error("Error getting lecturer ID: {}", e.getMessage());
        return null;
    }
}

// Add these methods to AttendanceService.java

public List<Map<String, Object>> getAvailableWeeksForCourse(String courseId) {
    String sql = """
        SELECT 
            wp.id as week_id,
            wp.week_number,
            wp.year,
            wp.start_date,
            wp.end_date,
            CASE 
                WHEN wp.end_date < CURDATE() THEN 'past'
                WHEN wp.start_date > CURDATE() THEN 'future'
                ELSE 'current'
            END as period_status
        FROM weekly_periods wp
        ORDER BY wp.year DESC, wp.week_number DESC
    """;
    
    return jdbc.queryForList(sql);
}

public List<Map<String, Object>> getStudentsForManualAttendance(String courseId, String weekId, String lecturerId) {
    String sql = """
        SELECT 
            s.user_id as studentId,
            s.student_number as studentNumber,
            CONCAT(u.first_name, ' ', u.last_name) as studentName,
            COALESCE(wca.sessions_attended, 0) as sessionsAttended,
            COALESCE(wca.sessions_held, 0) as totalSessions,
            COALESCE(wca.attendance_rate, 0) as attendanceRate,
            CASE 
                WHEN wca.status = 'regular' THEN 'present'
                ELSE COALESCE(wca.status, 'absent')
            END as autoStatus,
            ma.status as manualStatus,
            ma.notes,
            c.title as courseName,
            CASE 
                WHEN ma.status IS NOT NULL THEN 
                    CASE WHEN ma.status = 'present' OR ma.status = 'excused' THEN 'present' ELSE ma.status END
                ELSE 
                    CASE WHEN wca.status = 'regular' THEN 'present' ELSE 'absent' END
            END as finalStatus,
            CASE WHEN ma.status IS NOT NULL THEN TRUE ELSE FALSE END as isManuallyModified
        FROM course_students cs
        JOIN students s ON s.user_id = cs.student_id
        JOIN users u ON u.id = s.user_id
        JOIN courses c ON c.id = cs.course_id
        LEFT JOIN weekly_course_attendance wca ON wca.student_id = cs.student_id 
            AND wca.course_id = cs.course_id 
            AND wca.week_id = ?
        LEFT JOIN manual_attendance ma ON ma.student_id = cs.student_id 
            AND ma.course_id = cs.course_id 
            AND ma.week_id = ?
        WHERE cs.course_id = ? 
            AND cs.lecturer_id = ?
            AND cs.dropped_at IS NULL
        ORDER BY u.last_name, u.first_name
    """;
    
    return jdbc.queryForList(sql, weekId, weekId, courseId, lecturerId);
}
// In AttendanceService.java - Replace your saveManualAttendance method with this:

@Transactional
public void saveManualAttendance(String courseId, String weekId, List<Map<String, Object>> students, String lecturerId) {
    for (Map<String, Object> student : students) {
        String studentId = (String) student.get("studentId");
        String status = (String) student.get("status");
        String notes = (String) student.get("notes");
        
        // Convert status for manual_attendance table
        String manualStatus = "regular".equals(status) ? "present" : status;
        
        // Delete existing record first
        String deleteSql = "DELETE FROM manual_attendance WHERE student_id = ? AND course_id = ? AND week_id = ?";
        jdbc.update(deleteSql, studentId, courseId, weekId);
        
        // Insert new record
        String insertSql = """
            INSERT INTO manual_attendance (id, student_id, course_id, week_id, status, modified_by, notes, created_at, updated_at)
            VALUES (UUID(), ?, ?, ?, ?, ?, ?, NOW(), NOW())
        """;
        jdbc.update(insertSql, studentId, courseId, weekId, manualStatus, lecturerId, notes);
        
        // 🔥 EXCUSED COUNTS AS PRESENT - update weekly_course_attendance
        String weeklyStatus = "regular".equals(status) || "excused".equals(status) ? "regular" : status;
        
        String updateWeekly = """
            UPDATE weekly_course_attendance 
            SET status = ?, last_updated = NOW()
            WHERE student_id = ? AND course_id = ? AND week_id = ?
        """;
        int updated = jdbc.update(updateWeekly, weeklyStatus, studentId, courseId, weekId);
        
        if (updated == 0) {
            String insertWeekly = """
                INSERT INTO weekly_course_attendance 
                (id, week_id, student_id, course_id, sessions_held, sessions_attended, 
                 sessions_missed, attendance_rate, status, last_updated)
                VALUES (UUID(), ?, ?, ?, 0, 0, 0, 0, ?, NOW())
            """;
            jdbc.update(insertWeekly, weekId, studentId, courseId, weeklyStatus);
        }
        
        logger.info("Manual attendance saved for student: {} with status: {} -> weekly status: {}", 
                    studentId, status, weeklyStatus);
    }
}
}