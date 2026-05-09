package com.eduvision.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class LecturerAnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(LecturerAnalyticsService.class);
    private final JdbcTemplate jdbc;

    public LecturerAnalyticsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    @FunctionalInterface
    private interface SqlSupplier { Object get() throws Exception; }

    private void safeQuery(Map<String, Object> result, String key, SqlSupplier s) {
        try { result.put(key, s.get()); }
        catch (Exception e) { log.warn("Query '{}' failed: {}", key, e.getMessage()); result.put(key, new ArrayList<>()); }
    }

    /** Focus timeline for one session — bucketed by minute */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<Map<String, Object>> getSessionFocusTimeline(String sessionId) {
        try {
            return jdbc.queryForList("""
                SELECT DATE_FORMAT(ses.captured_at, '%H:%i') as timeBucket,
                       ROUND(AVG(CASE
                           WHEN ses.concentration = 'high'   THEN 85
                           WHEN ses.concentration = 'medium' THEN 55
                           WHEN ses.concentration = 'low'    THEN 25
                           ELSE 10 END), 1) as avgFocus,
                       COUNT(DISTINCT ses.student_id) as studentCount,
                       (SELECT ses2.emotion FROM student_emotion_snapshots ses2
                        WHERE ses2.session_id = ses.session_id
                          AND DATE_FORMAT(ses2.captured_at,'%H:%i') = DATE_FORMAT(ses.captured_at,'%H:%i')
                        GROUP BY ses2.emotion ORDER BY COUNT(*) DESC LIMIT 1) as dominantEmotion
                FROM student_emotion_snapshots ses
                WHERE ses.session_id = ?
                GROUP BY timeBucket
                ORDER BY timeBucket
            """, sessionId);
        } catch (Exception e) {
            log.error("Error in session focus timeline: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /** At-risk students for a course — low focus, high absences, high distraction */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<Map<String, Object>> getCourseAtRiskStudents(String courseId) {
        try {
            List<Map<String, Object>> students = jdbc.queryForList("""
                SELECT u.id as studentId,
                       CONCAT(u.first_name, ' ', u.last_name) as studentName,
                       ROUND(AVG(sls.avg_concentration) * 100, 1) as avgFocus,
                       ROUND(AVG(sls.attentive_percentage) * 100, 1) as avgAttentiveness,
                       ROUND(AVG(sls.pct_distracted) * 100, 1) as distractionRate,
                       SUM(CASE WHEN sa.status = 'absent' THEN 1 ELSE 0 END) as absences,
                       COUNT(DISTINCT ls.id) as totalSessions,
                       MAX(sls.dominant_emotion) as dominantEmotion
                FROM course_students cs
                JOIN users u ON u.id = cs.student_id
                LEFT JOIN lecture_sessions ls ON ls.course_id = cs.course_id AND ls.status = 'completed'
                LEFT JOIN session_attendance sa ON sa.student_id = cs.student_id AND sa.session_id = ls.id
                LEFT JOIN student_lecture_summaries sls ON sls.student_id = cs.student_id AND sls.session_id = ls.id
                WHERE cs.course_id = ? AND cs.dropped_at IS NULL
                GROUP BY cs.student_id, u.id, u.first_name, u.last_name
                ORDER BY avgFocus ASC
            """, courseId);

            // Compute risk level
            for (Map<String, Object> s : students) {
                double focus = s.get("avgFocus") instanceof Number n ? n.doubleValue() : 100;
                int absences = s.get("absences") instanceof Number n ? n.intValue() : 0;
                int total = s.get("totalSessions") instanceof Number n ? n.intValue() : 1;
                double absRate = total > 0 ? (absences * 100.0 / total) : 0;

                String risk;
                if (focus < 40 || absRate >= 40) risk = "HIGH";
                else if (focus < 60 || absRate >= 25) risk = "MEDIUM";
                else risk = "LOW";
                s.put("riskLevel", risk);
                s.put("absenceRate", Math.round(absRate * 10.0) / 10.0);
            }
            return students;
        } catch (Exception e) {
            log.error("Error in at-risk students: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /** Lecture-by-lecture comparison for a course */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<Map<String, Object>> getCourseLectureComparison(String courseId) {
        try {
            return jdbc.queryForList("""
                SELECT ls.id as sessionId,
                       COALESCE(ls.title, DATE_FORMAT(ls.actual_start, 'Lec %d/%m/%Y')) as title,
                       ls.actual_start as date,
                       COUNT(DISTINCT CASE WHEN sa.status IN ('present','late') THEN sa.student_id END) as studentsPresent,
                       COUNT(DISTINCT sa.student_id) as totalEnrolled,
                       ROUND(AVG(sls.avg_concentration) * 100, 1) as avgFocus,
                       ROUND(AVG(sls.attentive_percentage) * 100, 1) as avgAttentiveness,
                       COUNT(DISTINCT al.id) as alertCount,
                       ROUND(COUNT(DISTINCT CASE WHEN sa.status IN ('present','late') THEN sa.student_id END)
                             * 100.0 / NULLIF(COUNT(DISTINCT sa.student_id), 0), 1) as attendancePct
                FROM lecture_sessions ls
                LEFT JOIN session_attendance sa ON sa.session_id = ls.id
                LEFT JOIN student_lecture_summaries sls ON sls.session_id = ls.id
                LEFT JOIN alerts al ON al.session_id = ls.id
                WHERE ls.course_id = ? AND ls.status = 'completed'
                GROUP BY ls.id, ls.title, ls.actual_start
                ORDER BY ls.actual_start ASC
            """, courseId);
        } catch (Exception e) {
            log.error("Error in lecture comparison: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /** Behavioral patterns for a course: focus by session half, early leavers, student emotion patterns */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Map<String, Object> getCourseBehavioralPatterns(String courseId) {
        Map<String, Object> result = new LinkedHashMap<>();

        // Focus first half vs second half
        safeQuery(result, "focusBySessionHalf", () -> jdbc.queryForList("""
            SELECT CASE
                WHEN TIMESTAMPDIFF(MINUTE, ls.actual_start, ses.captured_at) <= 30
                THEN 'First 30 min' ELSE 'After 30 min' END as sessionHalf,
                ROUND(AVG(CASE
                    WHEN ses.concentration = 'high'   THEN 85
                    WHEN ses.concentration = 'medium' THEN 55
                    WHEN ses.concentration = 'low'    THEN 25
                    ELSE 10 END), 1) as avgFocus,
                COUNT(*) as snapshots
            FROM student_emotion_snapshots ses
            JOIN lecture_sessions ls ON ls.id = ses.session_id
            WHERE ls.course_id = ? AND ls.actual_start IS NOT NULL
            GROUP BY CASE WHEN TIMESTAMPDIFF(MINUTE, ls.actual_start, ses.captured_at) <= 30
                     THEN 'First 30 min' ELSE 'After 30 min' END
        """, courseId));

        // Student emotion patterns per student
        safeQuery(result, "studentEmotionPatterns", () -> jdbc.queryForList("""
            SELECT CONCAT(u.first_name, ' ', u.last_name) as studentName,
                   sls.dominant_emotion as dominantEmotion,
                   ROUND(AVG(sls.avg_concentration) * 100, 1) as avgFocus,
                   COUNT(*) as sessions
            FROM student_lecture_summaries sls
            JOIN users u ON u.id = sls.student_id
            WHERE sls.course_id = ?
            GROUP BY sls.student_id, u.first_name, u.last_name, sls.dominant_emotion
            ORDER BY AVG(sls.avg_concentration) ASC LIMIT 20
        """, courseId));

        // Early leavers using session_attendance.left_at (students who left before session ended)
        safeQuery(result, "exitPatterns", () -> jdbc.queryForList("""
            SELECT CONCAT(u.first_name, ' ', u.last_name) as studentName,
                   COUNT(*) as sessionsLeftEarly,
                   ROUND(AVG(TIMESTAMPDIFF(MINUTE, sa.joined_at, sa.left_at)), 1) as avgMinutesStayed
            FROM session_attendance sa
            JOIN users u ON u.id = sa.student_id
            JOIN lecture_sessions ls ON ls.id = sa.session_id
            WHERE ls.course_id = ?
              AND sa.left_at IS NOT NULL
              AND sa.joined_at IS NOT NULL
              AND sa.left_at < ls.actual_end
            GROUP BY sa.student_id, u.first_name, u.last_name
            ORDER BY sessionsLeftEarly DESC LIMIT 10
        """, courseId));

        // Engagement trend across lectures
        safeQuery(result, "engagementTrend", () -> jdbc.queryForList("""
            SELECT DATE_FORMAT(ls.actual_start, '%d/%m') as date,
                   ROUND(AVG(sls.avg_concentration) * 100, 1) as avgFocus,
                   ROUND(AVG(sls.attentive_percentage) * 100, 1) as avgAttentiveness
            FROM student_lecture_summaries sls
            JOIN lecture_sessions ls ON ls.id = sls.session_id
            WHERE ls.course_id = ? AND ls.status = 'completed'
            GROUP BY ls.id, ls.actual_start
            ORDER BY ls.actual_start ASC
        """, courseId));

        return result;
    }

    /** AI-style risk prediction for students in a course */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<Map<String, Object>> getCourseAIPredictions(String courseId) {
        try {
            List<Map<String, Object>> students = getCourseAtRiskStudents(courseId);
            for (Map<String, Object> s : students) {
                double focus = s.get("avgFocus") instanceof Number n ? n.doubleValue() : 50;
                double absRate = s.get("absenceRate") instanceof Number n ? n.doubleValue() : 0;
                double distraction = s.get("distractionRate") instanceof Number n ? n.doubleValue() : 0;

                // Simple scoring model
                double riskScore = (Math.max(0, 60 - focus) * 0.5)
                                 + (absRate * 0.35)
                                 + (distraction * 0.15);

                s.put("riskScore", Math.min(100, Math.round(riskScore * 10.0) / 10.0));
                s.put("likelyToAbsent", absRate >= 30 ? "Yes" : absRate >= 15 ? "Maybe" : "No");
                s.put("engagementDecreasing", focus < 50 ? "Yes" : focus < 65 ? "Possibly" : "No");
                s.put("atRiskOfFailing", riskScore >= 50 ? "High Risk" : riskScore >= 30 ? "Moderate" : "Low Risk");
            }
            students.sort((a, b) -> {
                double ra = a.get("riskScore") instanceof Number n ? n.doubleValue() : 0;
                double rb = b.get("riskScore") instanceof Number n ? n.doubleValue() : 0;
                return Double.compare(rb, ra);
            });
            return students;
        } catch (Exception e) {
            log.error("Error in AI predictions: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
