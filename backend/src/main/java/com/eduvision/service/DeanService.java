// C:\Users\john\Desktop\eduvision\backend\src\main\java\com\eduvision\service\DeanService.java
package com.eduvision.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class DeanService {

    private static final Logger logger = LoggerFactory.getLogger(DeanService.class);
    private final JdbcTemplate jdbc;

    public DeanService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * GET /api/v1/facade/dean/dashboard
     * Returns high-level department summary
     */
    public Map<String, Object> getDepartmentSummary() {
        Map<String, Object> result = new LinkedHashMap<>();
        
        try {
            // Total students
            Integer totalStudents = jdbc.queryForObject("SELECT COUNT(*) FROM students", Integer.class);
            result.put("totalStudents", totalStudents != null ? totalStudents : 0);
            
            // Total lecturers
            Integer totalLecturers = jdbc.queryForObject("SELECT COUNT(*) FROM lecturers", Integer.class);
            result.put("totalLecturers", totalLecturers != null ? totalLecturers : 0);
            
            // Total courses
            Integer totalCourses = jdbc.queryForObject("SELECT COUNT(*) FROM courses", Integer.class);
            result.put("totalCourses", totalCourses != null ? totalCourses : 0);
            
            // Total sessions
            Integer totalSessions = jdbc.queryForObject("SELECT COUNT(*) FROM lecture_sessions", Integer.class);
            result.put("totalSessions", totalSessions != null ? totalSessions : 0);
            
            // Active sessions
            Integer activeSessions = jdbc.queryForObject(
                "SELECT COUNT(*) FROM lecture_sessions WHERE status = 'active'", 
                Integer.class);
            result.put("activeSessions", activeSessions != null ? activeSessions : 0);
            
            // Average attendance from weekly_course_attendance
            Double avgAttendance = jdbc.queryForObject(
                "SELECT ROUND(AVG(attendance_rate), 1) FROM weekly_course_attendance WHERE attendance_rate IS NOT NULL", 
                Double.class);
            result.put("avgAttendance", avgAttendance != null ? avgAttendance : 0.0);
            
            // Average engagement from emotions (happy and surprised = engaged)
            Double avgEngagement = jdbc.queryForObject(
                "SELECT ROUND(AVG(CASE WHEN emotion IN ('happy', 'surprised') THEN 100 ELSE 0 END), 1) FROM student_emotion_snapshots",
                Double.class);
            result.put("avgEngagement", avgEngagement != null ? avgEngagement : 0.0);
            
        } catch (Exception e) {
            logger.error("Error getting department summary: {}", e.getMessage());
            result.put("totalStudents", 0);
            result.put("totalLecturers", 0);
            result.put("totalCourses", 0);
            result.put("totalSessions", 0);
            result.put("activeSessions", 0);
            result.put("avgAttendance", 0.0);
            result.put("avgEngagement", 0.0);
        }
        
        return result;
    }

    /**
     * GET /api/v1/facade/dean/lecturer-performance
     * Returns all lecturers ranked by performance
     */
    public List<Map<String, Object>> getLecturerPerformance() {
        List<Map<String, Object>> results = new ArrayList<>();
        
        try {
            String sql = """
                SELECT 
                    l.user_id as lecturerId,
                    CONCAT(u.first_name, ' ', u.last_name) as lecturerName,
                    u.email,
                    COALESCE(l.department, 'N/A') as department,
                    COUNT(DISTINCT c.code) as courseCount,
                    COUNT(DISTINCT ls.id) as sessionCount,
                    ROUND(COALESCE(sentiment.sentiment_score, 0), 1) as sentimentScore,
                    ROUND(COALESCE(conc.avg_concentration, 0) * 100, 1) as avgConcentration,
                    ROUND(COALESCE(att.avg_attendance, 0), 1) as avgAttendance
                FROM lecturers l
                JOIN users u ON u.id = l.user_id
                LEFT JOIN course_lecturers cl ON cl.lecturer_id = l.user_id
                LEFT JOIN courses c ON c.id = cl.course_id
                LEFT JOIN lecture_sessions ls ON ls.lecturer_id = l.user_id AND ls.status = 'completed'
                LEFT JOIN (
                    SELECT ls.lecturer_id, 
                           AVG(CASE WHEN ses.emotion IN ('happy', 'surprised') THEN 100 ELSE 0 END) as sentiment_score
                    FROM student_emotion_snapshots ses
                    JOIN lecture_sessions ls ON ls.id = ses.session_id
                    GROUP BY ls.lecturer_id
                ) sentiment ON sentiment.lecturer_id = l.user_id
                LEFT JOIN (
                    SELECT ls.lecturer_id,
                           AVG(CASE 
                               WHEN ses.concentration = 'high' THEN 0.9
                               WHEN ses.concentration = 'medium' THEN 0.6
                               WHEN ses.concentration = 'low' THEN 0.3
                               ELSE 0.5
                           END) as avg_concentration
                    FROM student_emotion_snapshots ses
                    JOIN lecture_sessions ls ON ls.id = ses.session_id
                    GROUP BY ls.lecturer_id
                ) conc ON conc.lecturer_id = l.user_id
                LEFT JOIN (
                    SELECT ls.lecturer_id,
                           AVG(wca.attendance_rate) as avg_attendance
                    FROM weekly_course_attendance wca
                    JOIN lecture_sessions ls ON ls.course_id = wca.course_id
                    GROUP BY ls.lecturer_id
                ) att ON att.lecturer_id = l.user_id
                GROUP BY l.user_id, u.first_name, u.last_name, u.email, l.department
                ORDER BY sentimentScore DESC
            """;
            
            results = jdbc.queryForList(sql);
            
            // Add rating based on sentiment score
            for (Map<String, Object> lecturer : results) {
                Double sentiment = null;
                Object sentimentObj = lecturer.get("sentimentScore");
                if (sentimentObj instanceof Number) {
                    sentiment = ((Number) sentimentObj).doubleValue();
                }
                
                String rating;
                if (sentiment == null || sentiment == 0) {
                    rating = "Insufficient Data";
                } else if (sentiment >= 70) {
                    rating = "Excellent";
                } else if (sentiment >= 50) {
                    rating = "Good";
                } else if (sentiment >= 30) {
                    rating = "Average";
                } else {
                    rating = "Needs Improvement";
                }
                lecturer.put("rating", rating);
                lecturer.put("sentimentScore", sentiment != null ? sentiment : 0);
            }
            
        } catch (Exception e) {
            logger.error("Error getting lecturer performance: {}", e.getMessage());
        }
        
        return results;
    }

    /**
     * GET /api/v1/facade/dean/course-stats
     * Returns per-course statistics
     */
    public List<Map<String, Object>> getCourseStats() {
        try {
            String sql = """
                SELECT 
                    c.id as courseId,
                    c.code,
                    c.title,
                    COALESCE(c.department, 'N/A') as department,
                    CONCAT(u.first_name, ' ', u.last_name) as lecturerName,
                    COUNT(DISTINCT cs.student_id) as enrolledStudents,
                    COUNT(DISTINCT ls.id) as sessionCount,
                    ROUND(COALESCE(eng.avg_engagement, 0), 1) as avgEngagement,
                    ROUND(COALESCE(att.avg_attendance, 0), 1) as avgAttendance
                FROM courses c
                LEFT JOIN course_lecturers cl ON cl.course_id = c.id
                LEFT JOIN lecturers l ON l.user_id = cl.lecturer_id
                LEFT JOIN users u ON u.id = l.user_id
                LEFT JOIN course_students cs ON cs.course_id = c.id AND cs.dropped_at IS NULL
                LEFT JOIN lecture_sessions ls ON ls.course_id = c.id
                LEFT JOIN (
                    SELECT ls.course_id,
                           AVG(CASE WHEN ses.emotion IN ('happy', 'surprised') THEN 100 ELSE 0 END) as avg_engagement
                    FROM student_emotion_snapshots ses
                    JOIN lecture_sessions ls ON ls.id = ses.session_id
                    GROUP BY ls.course_id
                ) eng ON eng.course_id = c.id
                LEFT JOIN (
                    SELECT wca.course_id,
                           AVG(wca.attendance_rate) as avg_attendance
                    FROM weekly_course_attendance wca
                    GROUP BY wca.course_id
                ) att ON att.course_id = c.id
                GROUP BY c.id, c.code, c.title, c.department, u.first_name, u.last_name
                ORDER BY c.department, c.code
            """;
            
            return jdbc.queryForList(sql);
            
        } catch (Exception e) {
            logger.error("Error getting course stats: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

        /**
     * GET /api/v1/facade/dean/weekly-trends
     * Returns weekly aggregated trends for last 12 weeks
     * FIXED: Uses week_id to join (not week_number)
     */
    public List<Map<String, Object>> getWeeklyTrends() {
        try {
            String sql = """
                SELECT 
                    wp.week_number,
                    wp.year,
                    CONCAT('W', wp.week_number, '/', wp.year) as label,
                    ROUND(AVG(wca.attendance_rate), 1) as avgAttendance,
                    COUNT(DISTINCT wca.student_id) as studentCount,
                    wp.start_date
                FROM weekly_periods wp
                LEFT JOIN weekly_course_attendance wca ON wca.week_id = wp.id
                WHERE wp.start_date >= DATE_SUB(CURDATE(), INTERVAL 12 WEEK)
                GROUP BY wp.id, wp.week_number, wp.year, wp.start_date
                ORDER BY wp.start_date ASC
                LIMIT 12
            """;
            
            return jdbc.queryForList(sql);
            
        } catch (Exception e) {
            logger.error("Error getting weekly trends: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Get course names for dropdown
     */
    public List<Map<String, Object>> getCourses() {
        try {
            String sql = "SELECT id as courseId, code, title FROM courses ORDER BY code";
            return jdbc.queryForList(sql);
        } catch (Exception e) {
            logger.error("Error getting courses: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    // ── DEAN REPORTS ──────────────────────────────────────────────────────────

    /** Attendance Analytics: top absent students, worst courses, weekly comparison, heatmap */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Map<String, Object> getAttendanceAnalytics() {
        Map<String, Object> result = new LinkedHashMap<>();

        safeQuery(result, "topAbsentStudents", () -> jdbc.queryForList("""
            SELECT CONCAT(u.first_name, ' ', u.last_name) as studentName,
                   COUNT(*) as totalSessions,
                   SUM(CASE WHEN sa.status = 'absent' THEN 1 ELSE 0 END) as absences,
                   ROUND(AVG(CASE WHEN sa.status = 'absent' THEN 100.0 ELSE 0 END), 1) as absenceRate
            FROM session_attendance sa
            JOIN users u ON u.id = sa.student_id
            GROUP BY sa.student_id, u.first_name, u.last_name
            HAVING SUM(CASE WHEN sa.status = 'absent' THEN 1 ELSE 0 END) > 0
            ORDER BY absenceRate DESC LIMIT 10
        """));

        safeQuery(result, "avgAttendancePerCourse", () -> jdbc.queryForList("""
            SELECT c.code, c.title,
                   ROUND(AVG(wca.attendance_rate), 1) as avgAttendance,
                   COUNT(DISTINCT wca.student_id) as studentCount
            FROM weekly_course_attendance wca
            JOIN courses c ON c.id = wca.course_id
            GROUP BY wca.course_id, c.code, c.title
            ORDER BY avgAttendance ASC
        """));

        safeQuery(result, "weeklyAttendance", () -> jdbc.queryForList("""
            SELECT wp.week_number, wp.year,
                   CONCAT('W', wp.week_number) as label,
                   ROUND(AVG(wca.attendance_rate), 1) as avgAttendance
            FROM weekly_periods wp
            JOIN weekly_course_attendance wca ON wca.week_id = wp.id
            GROUP BY wp.id, wp.week_number, wp.year, wp.start_date
            ORDER BY wp.start_date ASC LIMIT 12
        """));

        safeQuery(result, "attendanceHeatmap", () -> jdbc.queryForList("""
            SELECT DAYOFWEEK(ls.actual_start) as dayNum,
                   DAYNAME(ls.actual_start) as dayName,
                   HOUR(ls.actual_start) as hourOfDay,
                   ROUND(AVG(CASE WHEN sa.status = 'present' THEN 100.0 ELSE 0 END), 1) as attendanceRate,
                   COUNT(DISTINCT ls.id) as sessionCount
            FROM session_attendance sa
            JOIN lecture_sessions ls ON ls.id = sa.session_id
            WHERE ls.actual_start IS NOT NULL
            GROUP BY DAYOFWEEK(ls.actual_start), DAYNAME(ls.actual_start), HOUR(ls.actual_start)
            ORDER BY DAYOFWEEK(ls.actual_start), HOUR(ls.actual_start)
        """));

        return result;
    }

    /** Student Focus Report: avg focus per student, most distracted, low-focus courses, trend */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Map<String, Object> getStudentFocusReport() {
        Map<String, Object> result = new LinkedHashMap<>();

        safeQuery(result, "focusPerStudent", () -> jdbc.queryForList("""
            SELECT CONCAT(u.first_name, ' ', u.last_name) as studentName,
                   ROUND(AVG(sls.avg_concentration) * 100, 1) as avgFocus,
                   ROUND(AVG(sls.attentive_percentage) * 100, 1) as avgAttentiveness,
                   COUNT(sls.session_id) as sessionsAttended,
                   MAX(sls.dominant_emotion) as dominantEmotion
            FROM student_lecture_summaries sls
            JOIN users u ON u.id = sls.student_id
            GROUP BY sls.student_id, u.first_name, u.last_name
            ORDER BY AVG(sls.avg_concentration) ASC
        """));

        safeQuery(result, "mostDistractedStudents", () -> jdbc.queryForList("""
            SELECT CONCAT(u.first_name, ' ', u.last_name) as studentName,
                   ROUND(AVG(sls.avg_concentration) * 100, 1) as avgFocus,
                   ROUND(AVG(sls.pct_distracted) * 100, 1) as distractionRate
            FROM student_lecture_summaries sls
            JOIN users u ON u.id = sls.student_id
            GROUP BY sls.student_id, u.first_name, u.last_name
            ORDER BY AVG(sls.avg_concentration) ASC LIMIT 10
        """));

        safeQuery(result, "focusByCourse", () -> jdbc.queryForList("""
            SELECT c.code, c.title,
                   ROUND(AVG(sls.avg_concentration) * 100, 1) as avgFocus,
                   ROUND(AVG(sls.attentive_percentage) * 100, 1) as avgAttentiveness,
                   COUNT(DISTINCT sls.student_id) as studentCount
            FROM student_lecture_summaries sls
            JOIN courses c ON c.id = sls.course_id
            GROUP BY sls.course_id, c.code, c.title
            ORDER BY AVG(sls.avg_concentration) ASC
        """));

        safeQuery(result, "focusTrend", () -> jdbc.queryForList("""
            SELECT DATE_FORMAT(ls.actual_start, '%Y-%u') as weekKey,
                   DATE_FORMAT(ls.actual_start, '%d/%m') as label,
                   ROUND(AVG(sls.avg_concentration) * 100, 1) as avgFocus
            FROM student_lecture_summaries sls
            JOIN lecture_sessions ls ON ls.id = sls.session_id
            WHERE ls.actual_start IS NOT NULL
            GROUP BY weekKey, DATE_FORMAT(ls.actual_start, '%d/%m')
            ORDER BY weekKey ASC LIMIT 12
        """));

        return result;
    }

    /** Peak Activity Report: focus by hour/day, best/worst lecture slots */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Map<String, Object> getPeakActivityReport() {
        Map<String, Object> result = new LinkedHashMap<>();

        safeQuery(result, "focusByHour", () -> jdbc.queryForList("""
            SELECT HOUR(ses.captured_at) as hourOfDay,
                   ROUND(AVG(CASE
                       WHEN ses.concentration = 'high'   THEN 85
                       WHEN ses.concentration = 'medium' THEN 55
                       WHEN ses.concentration = 'low'    THEN 25
                       ELSE 10 END), 1) as avgFocus,
                   COUNT(*) as snapshotCount
            FROM student_emotion_snapshots ses
            GROUP BY HOUR(ses.captured_at)
            ORDER BY HOUR(ses.captured_at)
        """));

        safeQuery(result, "focusByDay", () -> jdbc.queryForList("""
            SELECT DAYOFWEEK(ses.captured_at) as dayNum,
                   DAYNAME(ses.captured_at) as dayName,
                   ROUND(AVG(CASE
                       WHEN ses.concentration = 'high'   THEN 85
                       WHEN ses.concentration = 'medium' THEN 55
                       WHEN ses.concentration = 'low'    THEN 25
                       ELSE 10 END), 1) as avgFocus
            FROM student_emotion_snapshots ses
            GROUP BY DAYOFWEEK(ses.captured_at), DAYNAME(ses.captured_at)
            ORDER BY DAYOFWEEK(ses.captured_at)
        """));

        safeQuery(result, "lectureSlots", () -> jdbc.queryForList("""
            SELECT c.code as courseCode,
                   DATE_FORMAT(ls.actual_start, '%a %H:%i') as slot,
                   ROUND(AVG(sls.avg_concentration) * 100, 1) as avgFocus,
                   ROUND(AVG(sls.attentive_percentage) * 100, 1) as avgAttentiveness,
                   COUNT(DISTINCT sls.session_id) as sessionCount
            FROM student_lecture_summaries sls
            JOIN lecture_sessions ls ON ls.id = sls.session_id
            JOIN courses c ON c.id = sls.course_id
            WHERE ls.actual_start IS NOT NULL
            GROUP BY c.code, DATE_FORMAT(ls.actual_start, '%a %H:%i')
            ORDER BY AVG(sls.avg_concentration) DESC LIMIT 20
        """));

        return result;
    }

    // ── Helper: run a query and store result or empty list on failure ──────────
    @FunctionalInterface
    private interface SqlSupplier { Object get() throws Exception; }

    private void safeQuery(Map<String, Object> result, String key, SqlSupplier supplier) {
        try {
            result.put(key, supplier.get());
        } catch (Exception e) {
            logger.warn("Query '{}' failed: {}", key, e.getMessage());
            result.put(key, new ArrayList<>());
        }
    }
}