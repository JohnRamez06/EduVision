// src/main/java/com/eduvision/service/AttendanceService.java
package com.eduvision.service;

import com.eduvision.dto.attendance.ExitRecordRequest;
import com.eduvision.dto.attendance.ExitLogDTO;
import com.eduvision.dto.attendance.ReturnRecordRequest;
import com.eduvision.dto.attendance.StudentSessionAttendanceDTO;
import com.eduvision.dto.attendance.WeeklyAttendanceDTO;
import com.eduvision.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
public class AttendanceService {

    private final JdbcTemplate jdbc;

    public AttendanceService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ------------------------------------------------------------------ exits

    @Transactional
    public void recordExit(ExitRecordRequest req) {
        // Verify student and session exist
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

        String id = java.util.UUID.randomUUID().toString();
        jdbc.update(
                "INSERT INTO session_exit_logs (exit_log_id, student_id, session_id, exit_time, exit_type) " +
                "VALUES (?, ?, ?, ?, ?)",
                id, req.getStudentId(), req.getSessionId(),
                LocalDateTime.now(), req.getExitType());
    }

    @Transactional
    public void recordReturn(ReturnRecordRequest req) {
        // Find the most recent open exit (no return_time) for this student/session
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

    // --------------------------------------------------------- session exits

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
                    LocalDateTime exit   = rs.getObject("exit_time",   LocalDateTime.class);
                    LocalDateTime ret    = rs.getObject("return_time", LocalDateTime.class);
                    long duration = (ret != null) ? ChronoUnit.MINUTES.between(exit, ret) : -1;
                    return new ExitLogDTO(
                            exit, ret,
                            duration >= 0 ? duration : null,
                            rs.getString("exit_type"),
                            rs.getString("student_name"));
                },
                sessionId);
    }

    // ------------------------------------------------------- student session

    public StudentSessionAttendanceDTO getStudentSessionAttendance(
            String studentId, String sessionId) {

        // Attendance record
        List<Map<String, Object>> attRows = jdbc.queryForList(
                "SELECT status, joined_at, left_at FROM session_attendance " +
                "WHERE student_id = ? AND session_id = ?",
                studentId, sessionId);

        boolean present = false;
        String status   = "absent";
        LocalDateTime joinedAt = null, leftAt = null;

        if (!attRows.isEmpty()) {
            Map<String, Object> att = attRows.get(0);
            status   = (String) att.get("status");
            present  = !"absent".equalsIgnoreCase(status);
            joinedAt = (LocalDateTime) att.get("joined_at");
            leftAt   = (LocalDateTime) att.get("left_at");
        }

        // Exit logs
        List<ExitLogDTO> exits = jdbc.query(
                "SELECT exit_time, return_time, exit_type FROM session_exit_logs " +
                "WHERE student_id = ? AND session_id = ? ORDER BY exit_time",
                (rs, row) -> {
                    LocalDateTime exit = rs.getObject("exit_time",   LocalDateTime.class);
                    LocalDateTime ret  = rs.getObject("return_time", LocalDateTime.class);
                    long mins = (ret != null) ? ChronoUnit.MINUTES.between(exit, ret) : -1;
                    return new ExitLogDTO(exit, ret, mins >= 0 ? mins : null,
                            rs.getString("exit_type"), null);
                },
                studentId, sessionId);

        return new StudentSessionAttendanceDTO(
                present, status, joinedAt, leftAt, exits.size(), exits);
    }

    // ---------------------------------------------------- weekly attendance

    public List<WeeklyAttendanceDTO> getWeeklyAttendance(String email, String weekId) {
        // Resolve student_id from email
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
                "JOIN courses c  ON c.course_id  = wca.course_id " +
                "JOIN students s ON s.student_id = wca.student_id " +
                "JOIN users u    ON u.user_id    = s.user_id " +
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
}