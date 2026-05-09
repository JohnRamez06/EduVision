package com.eduvision.facade;

import com.eduvision.dto.student.*;
import com.eduvision.service.RecommendationService;
import com.eduvision.service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.eduvision.dto.student.RecommendationDTO;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * FACADE PATTERN
 * Combines multiple service calls into a single cohesive API response,
 * so the frontend avoids making 4-5 individual round trips.
 *
 * Mapped under /api/v1/facade/student  (matches existing DashboardFacadeController pattern)
 */
@RestController
@RequestMapping("/api/v1/facade/student")
@PreAuthorize("hasRole('STUDENT')")
public class StudentPortalFacadeController {

    private final StudentService        studentService;
    private final RecommendationService recommendationService;
    private final JdbcTemplate          jdbc;

    public StudentPortalFacadeController(StudentService studentService,
                                          RecommendationService recommendationService,
                                          JdbcTemplate jdbc) {
        this.studentService        = studentService;
        this.recommendationService = recommendationService;
        this.jdbc                  = jdbc;
    }

    /**
     * GET /api/v1/facade/student/dashboard
     *
     * Aggregates in one call:
     *   1. Student profile
     *   2. Enrolled courses
     *   3. 5 most recent lecture summaries
     *   4. Overall engagement stats
     *   5. Personalised recommendations
     */
    @GetMapping("/dashboard")
    public ResponseEntity<StudentDashboardDTO> getDashboard() {

        String userId = studentService.getCurrentUser().getId();

        StudentDashboardDTO dashboard = new StudentDashboardDTO();
        dashboard.setStudentInfo(studentService.getProfile());
        dashboard.setEnrolledCourses(studentService.getEnrolledCourses());
        dashboard.setRecentSummaries(
                studentService.getLectureSummaries()
                        .stream().limit(5).toList());
        dashboard.setOverallStats(studentService.getOverallStats(userId));
        dashboard.setRecommendations(
                recommendationService.generateRecommendations(userId));

        return ResponseEntity.ok(dashboard);
    }

    /**
     * GET /api/v1/facade/student/lecture/{sessionId}/timeline
     *
     * Returns the per-snapshot concentration + emotion timeline for one session.
     * Used to render the timeline chart on the student's lecture detail page.
     */
    @GetMapping("/courses")
    public ResponseEntity<List<StudentDashboardDTO.EnrolledCourseDTO>> getCourses() {
        return ResponseEntity.ok(studentService.getEnrolledCourses());
    }

    @GetMapping("/summaries")
    public ResponseEntity<List<LectureSummaryDTO>> getSummaries() {
        return ResponseEntity.ok(studentService.getLectureSummaries());
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<RecommendationDTO>> getRecommendations() {
        String userId = studentService.getCurrentUser().getId();
        return ResponseEntity.ok(recommendationService.generateRecommendations(userId));
    }

    @GetMapping("/lecture/{sessionId}/timeline")
    public ResponseEntity<ConcentrationTimelineDTO> getConcentrationTimeline(
            @PathVariable String sessionId) {
        return ResponseEntity.ok(
                studentService.getConcentrationTimeline(sessionId));
    }

    /**
     * GET /api/v1/facade/student/courses/{courseId}/analytics
     *
     * Returns per-session concentration and emotion data for one course.
     * Used to render course-specific trend charts.
     */
    @GetMapping("/courses/{courseId}/analytics")
    public ResponseEntity<List<LectureSummaryDTO>> getCourseAnalytics(
            @PathVariable String courseId) {
        return ResponseEntity.ok(studentService.getCourseAnalytics(courseId));
    }

    /** GET /api/v1/facade/student/report/session/{sessionId}  — HTML report, auth via JWT */
    @GetMapping(value = "/report/session/{sessionId}", produces = MediaType.TEXT_HTML_VALUE)
    public org.springframework.http.ResponseEntity<String> sessionReport(
            @PathVariable String sessionId) {
        try {
            String studentId = studentService.getCurrentUser().getId();
            List<Map<String,Object>> rows = jdbc.queryForList(
                "SELECT sls.avg_concentration, sls.attentive_percentage, sls.dominant_emotion, " +
                "       c.title as course_name, c.code as course_code, " +
                "       ls.actual_start, " +
                "       CONCAT(u.first_name,' ',u.last_name) as student_name, " +
                "       sa.status as att_status " +
                "FROM student_lecture_summaries sls " +
                "JOIN courses c ON c.id = sls.course_id " +
                "JOIN lecture_sessions ls ON ls.id = sls.session_id " +
                "JOIN users u ON u.id = sls.student_id " +
                "LEFT JOIN session_attendance sa " +
                "       ON sa.session_id = sls.session_id AND sa.student_id = sls.student_id " +
                "WHERE sls.student_id = ? AND sls.session_id = ? LIMIT 1",
                studentId, sessionId);

            if (rows.isEmpty()) {
                return html("<div style='padding:40px;color:#EF4444;font-family:sans-serif;background:#0F172A;min-height:100vh'>" +
                    "<h2>No data found for this session.</h2>" +
                    "<p style='color:#94A3B8'>The session may not have been processed yet. " +
                    "Run the R compute script or wait for the next session to auto-compute.</p></div>");
            }

            Map<String,Object> r = rows.get(0);
            List<Map<String,Object>> snaps = jdbc.queryForList(
                "SELECT concentration, emotion FROM student_emotion_snapshots " +
                "WHERE student_id = ? AND session_id = ? ORDER BY captured_at", studentId, sessionId);

            return html(buildHtml(r, snaps));
        } catch (Exception e) {
            return html("<div style='padding:40px;color:#EF4444;font-family:sans-serif;background:#0F172A;min-height:100vh'>" +
                "<h2>Error</h2><p style='color:#94A3B8'>" + e.getMessage() + "</p></div>");
        }
    }

    /** GET /api/v1/facade/student/report/dashboard  — full HTML analytics report */
    @GetMapping(value = "/report/dashboard", produces = MediaType.TEXT_HTML_VALUE)
    public org.springframework.http.ResponseEntity<String> dashboardReport() {
        try {
            String studentId = studentService.getCurrentUser().getId();
            var profile  = studentService.getProfile();
            var stats    = studentService.getOverallStats(studentId);
            var summaries = studentService.getLectureSummaries();
            var courses  = studentService.getEnrolledCourses();

            StringBuilder sb = new StringBuilder(htmlHead("Analytics Report"));
            sb.append("<div class='hdr'><h1>Analytics Report</h1><p>")
              .append(esc(profile.getFullName())).append(" · ").append(esc(profile.getStudentNumber())).append("</p></div>");

            double conc = stats.getAvgConcentration() * 100;
            double att  = stats.getAvgAttentiveness() * 100;
            sb.append("<div class='sec'><h2>Overall</h2>");
            sb.append(bar("Avg Concentration", conc));
            sb.append(bar("Attentiveness", att));
            sb.append("<div class='krow'><span>Sessions Attended</span><strong>").append(stats.getTotalLecturesAttended()).append("</strong></div>");
            sb.append("<div class='krow'><span>Top Emotion</span><strong>").append(cap(stats.getMostFrequentEmotion())).append("</strong></div>");
            sb.append("</div>");

            if (!courses.isEmpty()) {
                sb.append("<div class='sec'><h2>Courses</h2><table>");
                sb.append("<tr><th>Course</th><th>Attended</th><th>Total</th><th>Rate</th></tr>");
                for (var c : courses) {
                    int rate = c.getTotalSessions() > 0 ? c.getAttendedSessions() * 100 / c.getTotalSessions() : 0;
                    sb.append("<tr><td>").append(esc(c.getTitle())).append("</td>")
                      .append("<td>").append(c.getAttendedSessions()).append("</td>")
                      .append("<td>").append(c.getTotalSessions()).append("</td>")
                      .append("<td style='color:").append(col(rate)).append("'>").append(rate).append("%</td></tr>");
                }
                sb.append("</table></div>");
            }

            if (!summaries.isEmpty()) {
                sb.append("<div class='sec'><h2>Recent Sessions</h2><table>");
                sb.append("<tr><th>Course</th><th>Date</th><th>Focus</th><th>Emotion</th><th>Attendance</th></tr>");
                for (var s : summaries.stream().limit(15).toList()) {
                    double c = s.getAvgConcentration() != null ? s.getAvgConcentration().doubleValue() * 100 : 0;
                    String d = s.getDate() != null ? s.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yy")) : "—";
                    sb.append("<tr><td>").append(esc(s.getCourseName())).append("</td>")
                      .append("<td>").append(d).append("</td>")
                      .append("<td style='color:").append(col(c)).append("'>").append((int)c).append("%</td>")
                      .append("<td>").append(cap(s.getDominantEmotion())).append("</td>")
                      .append("<td>").append(cap(s.getAttendance())).append("</td></tr>");
                }
                sb.append("</table></div>");
            }

            return html(sb.append(htmlFoot()).toString());
        } catch (Exception e) {
            return html("<div style='padding:40px;color:#EF4444;font-family:sans-serif;background:#0F172A'>" +
                "<h2>Error</h2><p>" + e.getMessage() + "</p></div>");
        }
    }

    // ── private HTML helpers ──────────────────────────────────────────────────

    private org.springframework.http.ResponseEntity<String> html(String body) {
        return org.springframework.http.ResponseEntity.ok()
            .contentType(MediaType.TEXT_HTML).body(body);
    }

    private String buildHtml(Map<String,Object> r, List<Map<String,Object>> snaps) {
        double conc = dbl(r.get("avg_concentration")) * 100;
        double att  = dbl(r.get("attentive_percentage")) * 100;
        String name = str(r.get("student_name"));
        String course = str(r.get("course_name")) + " (" + str(r.get("course_code")) + ")";
        String date = fmtDate(r.get("actual_start"));

        StringBuilder sb = new StringBuilder(htmlHead("Session Report – " + name));
        sb.append("<div class='hdr'><h1>Session Report</h1>")
          .append("<p><strong>Student:</strong> ").append(esc(name)).append("</p>")
          .append("<p><strong>Course:</strong> ").append(esc(course)).append("</p>")
          .append("<p><strong>Date:</strong> ").append(date)
          .append(" · <strong>Attendance:</strong> ").append(cap(str(r.get("att_status")))).append("</p></div>");

        sb.append("<div class='sec'><h2>Performance</h2>");
        sb.append(bar("Average Concentration", conc));
        sb.append(bar("Attentiveness", att));
        sb.append("<div class='krow'><span>Dominant Emotion</span><strong>").append(cap(str(r.get("dominant_emotion")))).append("</strong></div>");
        sb.append("</div>");

        if (!snaps.isEmpty()) {
            java.util.Map<String,Long> ec = new java.util.LinkedHashMap<>();
            for (var s : snaps) ec.merge(str(s.get("emotion")).toLowerCase(), 1L, Long::sum);
            long total = ec.values().stream().mapToLong(Long::longValue).sum();
            sb.append("<div class='sec'><h2>Emotion Breakdown</h2>");
            ec.entrySet().stream().sorted((a,b)->Long.compare(b.getValue(),a.getValue())).forEach(e -> {
                int p = total > 0 ? (int)(e.getValue()*100/total) : 0;
                sb.append("<div class='emo'><span class='el'>").append(cap(e.getKey())).append("</span>")
                  .append("<div class='bb'><div class='bf' style='width:").append(p).append("%;background:").append(ec2(e.getKey())).append("'></div></div>")
                  .append("<span class='ep'>").append(p).append("%</span></div>");
            });
            sb.append("</div>");

            sb.append("<div class='sec'><h2>Concentration Timeline</h2><div class='tl'>");
            for (var s : snaps) {
                int h = switch(str(s.get("concentration")).toLowerCase()) {
                    case "high" -> 80; case "medium" -> 55; case "low" -> 30; default -> 10;
                };
                sb.append("<div class='tb' style='height:").append(h).append("px;background:").append(col(h)).append("'></div>");
            }
            sb.append("</div><p style='color:#64748B;font-size:11px;margin-top:4px'>Each bar = one snapshot</p></div>");
        }

        return sb.append(htmlFoot()).toString();
    }

    private String htmlHead(String title) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>" + esc(title) + "</title><style>" +
            "body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;margin:0;padding:20px 24px;background:#0F172A;color:#E2E8F0}" +
            "h1{font-size:20px;font-weight:700;color:#fff;margin:0 0 6px}" +
            "h2{font-size:12px;font-weight:600;color:#ACBBC6;margin:0 0 12px;padding-bottom:6px;border-bottom:1px solid #334155;text-transform:uppercase;letter-spacing:.05em}" +
            ".hdr{background:linear-gradient(135deg,#16254F,#2D4A7A);padding:18px 22px;border-radius:12px;margin-bottom:14px}" +
            ".hdr p{margin:4px 0;color:#ACBBC6;font-size:13px}" +
            ".sec{background:#1E293B;border:1px solid #334155;border-radius:12px;padding:16px;margin-bottom:12px}" +
            ".krow{display:flex;justify-content:space-between;padding:8px 0;border-bottom:1px solid #1E293B;font-size:13px}" +
            ".krow:last-child{border:none}" +
            ".brow{margin-bottom:10px}" +
            ".blbl{display:flex;justify-content:space-between;font-size:12px;margin-bottom:3px;color:#94A3B8}" +
            ".bg{background:#334155;border-radius:4px;height:7px;overflow:hidden}" +
            ".bf2{height:100%;border-radius:4px}" +
            ".emo{display:flex;align-items:center;gap:8px;margin-bottom:7px}" +
            ".el{width:75px;font-size:12px;color:#94A3B8;text-transform:capitalize;flex-shrink:0}" +
            ".bb{flex:1;background:#334155;border-radius:3px;height:7px;overflow:hidden}" +
            ".bf{height:100%;border-radius:3px}" +
            ".ep{width:30px;text-align:right;font-size:12px;font-weight:600}" +
            ".tl{display:flex;align-items:flex-end;gap:2px;height:80px;overflow-x:auto}" +
            ".tb{width:7px;border-radius:2px 2px 0 0;min-height:3px;flex-shrink:0}" +
            "table{width:100%;border-collapse:collapse;font-size:12px}" +
            "th{text-align:left;padding:7px 10px;font-size:10px;color:#64748B;text-transform:uppercase;border-bottom:1px solid #334155}" +
            "td{padding:8px 10px;border-bottom:1px solid #1E293B;color:#CBD5E1}" +
            "tr:hover td{background:#0F172A}" +
            ".btn{background:#667D9D;color:#fff;border:none;padding:7px 16px;border-radius:8px;cursor:pointer;font-size:12px;margin-bottom:14px}" +
            "@media print{.btn{display:none}}" +
            "</style></head><body>" +
            "<div style='text-align:right'><button class='btn' onclick='window.print()'>🖨 Print / Save as PDF</button></div>";
    }

    private String htmlFoot() {
        return "<div style='text-align:center;color:#475569;font-size:11px;margin-top:18px;padding-top:12px;border-top:1px solid #334155'>" +
               "EduVision Analytics · " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")) +
               "</div></body></html>";
    }

    private String bar(String label, double pct) {
        return "<div class='brow'><div class='blbl'><span>" + label + "</span>" +
               "<span style='color:" + col(pct) + ";font-weight:700'>" + (int)pct + "%</span></div>" +
               "<div class='bg'><div class='bf2' style='width:" + Math.min((int)pct,100) + "%;background:" + col(pct) + "'></div></div></div>";
    }

    private String col(double p)  { return p >= 70 ? "#10B981" : p >= 45 ? "#F59E0B" : "#EF4444"; }
    private String ec2(String e)  {
        return switch(e) { case "happy"->"#10B981"; case "engaged"->"#3B82F6"; case "neutral"->"#94A3B8";
                           case "confused"->"#F59E0B"; case "sad"->"#8B5CF6"; case "angry"->"#EF4444"; default->"#667D9D"; };
    }
    private String cap(String s)  { return s==null||s.isEmpty()?"—":Character.toUpperCase(s.charAt(0))+s.substring(1).toLowerCase(); }
    private String esc(String s)  { return s==null?"":s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;"); }
    private String str(Object o)  { return o==null?"—":o.toString(); }
    private double dbl(Object o)  { if(o==null)return 0;if(o instanceof Number n)return n.doubleValue();try{return Double.parseDouble(o.toString());}catch(Exception e){return 0;} }
    private String fmtDate(Object o) {
        if(o==null)return"—";
        try{ if(o instanceof java.sql.Timestamp t)return t.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
             return o.toString().substring(0,Math.min(16,o.toString().length()));}
        catch(Exception e){return o.toString();}
    }
}