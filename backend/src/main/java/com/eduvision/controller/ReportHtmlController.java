package com.eduvision.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Generates HTML reports directly from DB using JdbcTemplate.
 * No R, no pandoc, no JWT required — just pass IDs in the URL.
 *
 * Endpoints (all public):
 *   GET /api/v1/html-reports/student/{studentId}/session/{sessionId}
 *   GET /api/v1/html-reports/student/{studentId}/dashboard
 *   GET /api/v1/html-reports/lecturer/{lecturerId}/course/{courseId}
 */
@RestController
@RequestMapping("/api/v1/html-reports")
public class ReportHtmlController {

    private final JdbcTemplate jdbc;

    public ReportHtmlController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ── Student session report ────────────────────────────────────────────────

    @GetMapping(value = "/student/{studentId}/session/{sessionId}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> studentSession(
            @PathVariable String studentId,
            @PathVariable String sessionId) {
        try {
            // Summary
            List<Map<String,Object>> rows = jdbc.queryForList(
                "SELECT sls.avg_concentration, sls.attentive_percentage, sls.dominant_emotion, " +
                "       sls.pct_happy, sls.pct_sad, sls.pct_angry, sls.pct_confused, " +
                "       sls.pct_neutral, sls.pct_engaged, sls.pct_distracted, sls.snapshot_count, " +
                "       c.title as course_name, c.code as course_code, " +
                "       ls.actual_start, " +
                "       CONCAT(u.first_name,' ',u.last_name) as student_name, " +
                "       sa.status as attendance_status " +
                "FROM student_lecture_summaries sls " +
                "JOIN courses c ON c.id = sls.course_id " +
                "JOIN lecture_sessions ls ON ls.id = sls.session_id " +
                "JOIN users u ON u.id = sls.student_id " +
                "LEFT JOIN session_attendance sa ON sa.session_id = sls.session_id AND sa.student_id = sls.student_id " +
                "WHERE sls.student_id = ? AND sls.session_id = ? LIMIT 1",
                studentId, sessionId);

            if (rows.isEmpty()) {
                return ok(errorHtml("No data found for this session. Make sure the session has been processed."));
            }

            Map<String,Object> r = rows.get(0);

            // Concentration timeline from raw snapshots
            List<Map<String,Object>> snaps = jdbc.queryForList(
                "SELECT concentration, emotion, captured_at FROM student_emotion_snapshots " +
                "WHERE student_id = ? AND session_id = ? ORDER BY captured_at",
                studentId, sessionId);

            String html = buildSessionReport(r, snaps);
            return ok(html);
        } catch (Exception e) {
            return ok(errorHtml("Error: " + e.getMessage()));
        }
    }

    // ── Student full dashboard report ─────────────────────────────────────────

    @GetMapping(value = "/student/{studentId}/dashboard", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> studentDashboard(@PathVariable String studentId) {
        try {
            // Overall stats
            Map<String,Object> overall = jdbc.queryForMap(
                "SELECT CONCAT(u.first_name,' ',u.last_name) as student_name, " +
                "       s.student_number, s.program, " +
                "       ROUND(AVG(sls.avg_concentration)*100,1) as avg_conc, " +
                "       ROUND(AVG(sls.attentive_percentage)*100,1) as avg_att, " +
                "       COUNT(DISTINCT sls.session_id) as total_sessions " +
                "FROM users u " +
                "JOIN students s ON s.user_id = u.id " +
                "LEFT JOIN student_lecture_summaries sls ON sls.student_id = u.id " +
                "WHERE u.id = ? " +
                "GROUP BY u.id, u.first_name, u.last_name, s.student_number, s.program",
                studentId);

            // Sessions
            List<Map<String,Object>> sessions = jdbc.queryForList(
                "SELECT c.title as course_name, c.code, ls.actual_start, " +
                "       ROUND(sls.avg_concentration*100,1) as conc_pct, " +
                "       sls.dominant_emotion, sa.status as attendance " +
                "FROM student_lecture_summaries sls " +
                "JOIN courses c ON c.id = sls.course_id " +
                "JOIN lecture_sessions ls ON ls.id = sls.session_id " +
                "LEFT JOIN session_attendance sa ON sa.session_id = sls.session_id AND sa.student_id = sls.student_id " +
                "WHERE sls.student_id = ? ORDER BY ls.actual_start DESC LIMIT 20",
                studentId);

            // Emotion totals
            Map<String,Object> emotions = new LinkedHashMap<>();
            try {
                List<Map<String,Object>> emoRows = jdbc.queryForList(
                    "SELECT emotion, COUNT(*) as cnt FROM student_emotion_snapshots " +
                    "WHERE student_id = ? GROUP BY emotion ORDER BY cnt DESC", studentId);
                long total = emoRows.stream().mapToLong(e -> num(e.get("cnt")).longValue()).sum();
                for (var emo : emoRows) {
                    double pct = total > 0 ? num(emo.get("cnt")).doubleValue() * 100 / total : 0;
                    emotions.put(str(emo.get("emotion")), pct);
                }
            } catch (Exception ignored) {}

            return ok(buildDashboardReport(overall, sessions, emotions));
        } catch (Exception e) {
            return ok(errorHtml("Error: " + e.getMessage()));
        }
    }

    // ── Lecturer course report ────────────────────────────────────────────────

    @GetMapping(value = "/lecturer/{lecturerId}/course/{courseId}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> lecturerCourse(
            @PathVariable String lecturerId,
            @PathVariable String courseId) {
        try {
            Map<String,Object> courseInfo = jdbc.queryForMap(
                "SELECT c.title, c.code FROM courses c WHERE c.id = ?", courseId);

            List<Map<String,Object>> lectures = jdbc.queryForList(
                "SELECT ls.id as session_id, ls.actual_start, " +
                "       COUNT(DISTINCT CASE WHEN sa.status IN ('present','late') THEN sa.student_id END) as present, " +
                "       COUNT(DISTINCT sa.student_id) as enrolled, " +
                "       ROUND(AVG(sls.avg_concentration)*100,1) as avg_focus, " +
                "       ROUND(AVG(sls.attentive_percentage)*100,1) as avg_att, " +
                "       COUNT(DISTINCT al.id) as alerts " +
                "FROM lecture_sessions ls " +
                "LEFT JOIN session_attendance sa ON sa.session_id = ls.id " +
                "LEFT JOIN student_lecture_summaries sls ON sls.session_id = ls.id " +
                "LEFT JOIN alerts al ON al.session_id = ls.id " +
                "WHERE ls.course_id = ? AND ls.status = 'completed' " +
                "GROUP BY ls.id, ls.actual_start ORDER BY ls.actual_start DESC",
                courseId);

            List<Map<String,Object>> atRisk = jdbc.queryForList(
                "SELECT CONCAT(u.first_name,' ',u.last_name) as name, " +
                "       ROUND(AVG(sls.avg_concentration)*100,1) as avg_focus, " +
                "       SUM(CASE WHEN sa.status='absent' THEN 1 ELSE 0 END) as absences " +
                "FROM course_students cs " +
                "JOIN users u ON u.id = cs.student_id " +
                "LEFT JOIN lecture_sessions ls ON ls.course_id = cs.course_id AND ls.status='completed' " +
                "LEFT JOIN session_attendance sa ON sa.student_id = cs.student_id AND sa.session_id = ls.id " +
                "LEFT JOIN student_lecture_summaries sls ON sls.student_id = cs.student_id AND sls.session_id = ls.id " +
                "WHERE cs.course_id = ? AND cs.dropped_at IS NULL " +
                "GROUP BY cs.student_id, u.first_name, u.last_name " +
                "ORDER BY avg_focus ASC LIMIT 10", courseId);

            return ok(buildLecturerCourseReport(courseInfo, lectures, atRisk));
        } catch (Exception e) {
            return ok(errorHtml("Error: " + e.getMessage()));
        }
    }

    // ── HTML builders ─────────────────────────────────────────────────────────

    private String buildSessionReport(Map<String,Object> r, List<Map<String,Object>> snaps) {
        double conc = num(r.get("avg_concentration")).doubleValue() * 100;
        double att  = num(r.get("attentive_percentage")).doubleValue() * 100;
        String emo  = cap(str(r.get("dominant_emotion")));
        String name = str(r.get("student_name"));
        String course = str(r.get("course_name")) + " (" + str(r.get("course_code")) + ")";
        String date = formatDate(r.get("actual_start"));
        String attendance = cap(str(r.get("attendance_status")));

        StringBuilder sb = new StringBuilder(head("Session Report – " + name));
        sb.append("<div class='hdr'><h1>Session Report</h1>");
        sb.append("<p><strong>Student:</strong> ").append(esc(name)).append("</p>");
        sb.append("<p><strong>Course:</strong> ").append(esc(course)).append("</p>");
        sb.append("<p><strong>Date:</strong> ").append(date).append(" &nbsp;·&nbsp; <strong>Attendance:</strong> ").append(attendance).append("</p>");
        sb.append("</div>");

        sb.append("<div class='sec'><h2>Performance</h2>");
        sb.append(bar("Average Concentration", conc));
        sb.append(bar("Attentiveness",          att));
        sb.append("<div class='krow'><span>Dominant Emotion</span><strong>").append(emo).append("</strong></div>");
        sb.append("</div>");

        // Emotion breakdown from raw snapshots
        if (!snaps.isEmpty()) {
            Map<String,Long> emoCounts = new LinkedHashMap<>();
            for (var s : snaps) {
                String e = str(s.get("emotion")).toLowerCase();
                emoCounts.merge(e, 1L, Long::sum);
            }
            long total = emoCounts.values().stream().mapToLong(Long::longValue).sum();
            sb.append("<div class='sec'><h2>Emotion Breakdown</h2>");
            emoCounts.entrySet().stream()
                .sorted((a,b) -> Long.compare(b.getValue(), a.getValue()))
                .forEach(e -> {
                    double pct = total > 0 ? e.getValue() * 100.0 / total : 0;
                    sb.append("<div class='emo-row'>")
                      .append("<span class='emo-lbl'>").append(cap(e.getKey())).append("</span>")
                      .append("<div class='bb'><div class='bf' style='width:").append((int)pct).append("%;background:").append(emoColor(e.getKey())).append("'></div></div>")
                      .append("<span class='emo-pct'>").append((int)pct).append("%</span></div>");
                });
            sb.append("</div>");

            // Timeline
            sb.append("<div class='sec'><h2>Concentration Timeline</h2>");
            sb.append("<div class='tl'>");
            for (var s : snaps) {
                double score = concScore(str(s.get("concentration")));
                sb.append("<div class='tb' style='height:").append((int)score).append("px;background:").append(colorHex(score)).append("' title='").append((int)score).append("%'></div>");
            }
            sb.append("</div>");
            sb.append("<p style='color:#64748B;font-size:11px;margin-top:4px'>Each bar = one snapshot &nbsp;·&nbsp; <span style='color:#10B981'>■</span> High &nbsp;<span style='color:#F59E0B'>■</span> Medium &nbsp;<span style='color:#EF4444'>■</span> Low</p>");
            sb.append("</div>");
        }

        sb.append(footer());
        return sb.toString();
    }

    private String buildDashboardReport(Map<String,Object> overall, List<Map<String,Object>> sessions, Map<String,Object> emotions) {
        String name = str(overall.get("student_name"));
        double conc = num(overall.get("avg_conc")).doubleValue();
        double att  = num(overall.get("avg_att")).doubleValue();
        int    total = num(overall.get("total_sessions")).intValue();

        StringBuilder sb = new StringBuilder(head("Analytics Report – " + name));
        sb.append("<div class='hdr'><h1>Analytics Report</h1>");
        sb.append("<p><strong>").append(esc(name)).append("</strong> &nbsp;·&nbsp; ")
          .append(esc(str(overall.get("program")))).append(" &nbsp;·&nbsp; ")
          .append(esc(str(overall.get("student_number")))).append("</p></div>");

        sb.append("<div class='sec'><h2>Overall Performance</h2>");
        sb.append("<div class='kgrid'>");
        sb.append(kpi("Avg Concentration", pct(conc), colorHex(conc)));
        sb.append(kpi("Avg Attentiveness", pct(att),  colorHex(att)));
        sb.append(kpi("Sessions Attended", String.valueOf(total), "#667D9D"));
        sb.append("</div>");
        sb.append(bar("Average Concentration", conc));
        sb.append(bar("Attentiveness", att));
        sb.append("</div>");

        if (!emotions.isEmpty()) {
            sb.append("<div class='sec'><h2>Emotion Breakdown</h2>");
            emotions.forEach((emo, pct) -> {
                double p = ((Number)pct).doubleValue();
                sb.append("<div class='emo-row'>")
                  .append("<span class='emo-lbl'>").append(cap(emo)).append("</span>")
                  .append("<div class='bb'><div class='bf' style='width:").append((int)p).append("%;background:").append(emoColor(emo)).append("'></div></div>")
                  .append("<span class='emo-pct'>").append((int)p).append("%</span></div>");
            });
            sb.append("</div>");
        }

        if (!sessions.isEmpty()) {
            sb.append("<div class='sec'><h2>Session History</h2><table>");
            sb.append("<tr><th>Course</th><th>Date</th><th>Focus</th><th>Emotion</th><th>Attendance</th></tr>");
            for (var s : sessions) {
                double c = num(s.get("conc_pct")).doubleValue();
                sb.append("<tr><td>").append(esc(str(s.get("course_name")))).append("</td>")
                  .append("<td>").append(formatDate(s.get("actual_start"))).append("</td>")
                  .append("<td style='color:").append(colorHex(c)).append("'>").append(pct(c)).append("</td>")
                  .append("<td>").append(cap(str(s.get("dominant_emotion")))).append("</td>")
                  .append("<td>").append(cap(str(s.get("attendance")))).append("</td></tr>");
            }
            sb.append("</table></div>");
        }

        sb.append(footer());
        return sb.toString();
    }

    private String buildLecturerCourseReport(Map<String,Object> course, List<Map<String,Object>> lectures, List<Map<String,Object>> atRisk) {
        StringBuilder sb = new StringBuilder(head("Course Report – " + str(course.get("title"))));
        sb.append("<div class='hdr'><h1>Course Report</h1>");
        sb.append("<p><strong>").append(esc(str(course.get("title")))).append("</strong> (").append(esc(str(course.get("code")))).append(")</p></div>");

        if (!lectures.isEmpty()) {
            sb.append("<div class='sec'><h2>Lecture Comparison</h2><table>");
            sb.append("<tr><th>Date</th><th>Present</th><th>Enrolled</th><th>Avg Focus</th><th>Attentiveness</th><th>Alerts</th></tr>");
            for (var l : lectures) {
                double f = num(l.get("avg_focus")).doubleValue();
                double a = num(l.get("avg_att")).doubleValue();
                sb.append("<tr><td>").append(formatDate(l.get("actual_start"))).append("</td>")
                  .append("<td>").append(str(l.get("present"))).append("</td>")
                  .append("<td>").append(str(l.get("enrolled"))).append("</td>")
                  .append("<td style='color:").append(colorHex(f)).append("'>").append(pct(f)).append("</td>")
                  .append("<td style='color:").append(colorHex(a)).append("'>").append(pct(a)).append("</td>")
                  .append("<td>").append(str(l.get("alerts"))).append("</td></tr>");
            }
            sb.append("</table></div>");
        }

        if (!atRisk.isEmpty()) {
            sb.append("<div class='sec'><h2>Students Needing Support (lowest focus)</h2><table>");
            sb.append("<tr><th>Student</th><th>Avg Focus</th><th>Absences</th></tr>");
            for (var s : atRisk) {
                double f = num(s.get("avg_focus")).doubleValue();
                sb.append("<tr><td>").append(esc(str(s.get("name")))).append("</td>")
                  .append("<td style='color:").append(colorHex(f)).append("'>").append(pct(f)).append("</td>")
                  .append("<td>").append(str(s.get("absences"))).append("</td></tr>");
            }
            sb.append("</table></div>");
        }

        sb.append(footer());
        return sb.toString();
    }

    // ── HTML primitives ───────────────────────────────────────────────────────

    private String head(String title) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>" + esc(title) + "</title><style>" +
            "body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;margin:0;padding:20px 24px;background:#0F172A;color:#E2E8F0}" +
            "h1{font-size:20px;font-weight:700;color:#fff;margin:0 0 6px}" +
            "h2{font-size:13px;font-weight:600;color:#ACBBC6;margin:0 0 12px;padding-bottom:6px;border-bottom:1px solid #334155;text-transform:uppercase;letter-spacing:.05em}" +
            ".hdr{background:linear-gradient(135deg,#16254F,#2D4A7A);padding:18px 22px;border-radius:12px;margin-bottom:16px}" +
            ".hdr p{margin:4px 0;color:#ACBBC6;font-size:13px}" +
            ".sec{background:#1E293B;border:1px solid #334155;border-radius:12px;padding:18px;margin-bottom:14px}" +
            ".kgrid{display:grid;grid-template-columns:repeat(3,1fr);gap:10px;margin-bottom:16px}" +
            ".kcard{background:#0F172A;border:1px solid #334155;border-radius:8px;padding:12px;text-align:center}" +
            ".kval{font-size:22px;font-weight:700;margin:2px 0}" +
            ".klbl{font-size:10px;color:#64748B}" +
            ".krow{display:flex;justify-content:space-between;padding:9px 0;border-bottom:1px solid #1E293B;font-size:13px}" +
            ".krow:last-child{border:none}" +
            ".brow{margin-bottom:10px}" +
            ".blbl{display:flex;justify-content:space-between;font-size:12px;margin-bottom:3px;color:#94A3B8}" +
            ".bg{background:#334155;border-radius:4px;height:7px;overflow:hidden}" +
            ".bf2{height:100%;border-radius:4px}" +
            ".emo-row{display:flex;align-items:center;gap:8px;margin-bottom:7px}" +
            ".emo-lbl{width:80px;font-size:12px;color:#94A3B8;text-transform:capitalize;flex-shrink:0}" +
            ".bb{flex:1;background:#334155;border-radius:3px;height:7px;overflow:hidden}" +
            ".bf{height:100%;border-radius:3px}" +
            ".emo-pct{width:32px;text-align:right;font-size:12px;font-weight:600}" +
            ".tl{display:flex;align-items:flex-end;gap:2px;height:80px;overflow-x:auto;padding-bottom:2px}" +
            ".tb{width:7px;border-radius:2px 2px 0 0;min-height:3px;flex-shrink:0}" +
            "table{width:100%;border-collapse:collapse;font-size:12px}" +
            "th{text-align:left;padding:7px 10px;font-size:10px;color:#64748B;text-transform:uppercase;border-bottom:1px solid #334155}" +
            "td{padding:9px 10px;border-bottom:1px solid #1E293B;color:#CBD5E1}" +
            "tr:hover td{background:#0F172A}" +
            ".btn{background:#667D9D;color:#fff;border:none;padding:7px 16px;border-radius:8px;cursor:pointer;font-size:12px;margin-bottom:16px}" +
            "@media print{.btn{display:none}}" +
            "</style></head><body>" +
            "<div style='text-align:right'><button class='btn' onclick='window.print()'>🖨 Print / Save as PDF</button></div>";
    }

    private String bar(String label, double pct) {
        return "<div class='brow'><div class='blbl'><span>" + label + "</span><span style='color:" + colorHex(pct) + ";font-weight:700'>" + pct(pct) + "</span></div>" +
               "<div class='bg'><div class='bf2' style='width:" + Math.min((int)pct,100) + "%;background:" + colorHex(pct) + "'></div></div></div>";
    }

    private String kpi(String label, String value, String color) {
        return "<div class='kcard'><div class='kval' style='color:" + color + "'>" + value + "</div><div class='klbl'>" + label + "</div></div>";
    }

    private String footer() {
        return "<div style='text-align:center;color:#475569;font-size:11px;margin-top:20px;padding-top:14px;border-top:1px solid #334155'>" +
               "EduVision Analytics &nbsp;·&nbsp; " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")) +
               "</div></body></html>";
    }

    private String errorHtml(String msg) {
        return head("Error") +
               "<div style='background:#450A0A;border:1px solid #EF4444;border-radius:10px;padding:18px;color:#FCA5A5'>" +
               "<strong>Error:</strong> " + esc(msg) + "</div>" + footer();
    }

    private ResponseEntity<String> ok(String html) {
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String colorHex(double p) {
        return p >= 70 ? "#10B981" : p >= 45 ? "#F59E0B" : "#EF4444";
    }

    private String emoColor(String e) {
        return switch (e.toLowerCase()) {
            case "happy"    -> "#10B981";
            case "engaged"  -> "#3B82F6";
            case "neutral"  -> "#94A3B8";
            case "confused" -> "#F59E0B";
            case "sad"      -> "#8B5CF6";
            case "angry"    -> "#EF4444";
            case "surprised"-> "#06B6D4";
            default         -> "#667D9D";
        };
    }

    private double concScore(String level) {
        return switch (level.toLowerCase()) {
            case "high"       -> 80;
            case "medium"     -> 55;
            case "low"        -> 30;
            case "distracted" -> 10;
            default           -> 40;
        };
    }

    private String pct(double v) { return String.format("%.0f%%", v); }
    private String cap(String s) { return s == null || s.isEmpty() ? "—" : Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase(); }
    private String esc(String s) { return s == null ? "" : s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;"); }
    private String str(Object o) { return o == null ? "—" : o.toString(); }
    private Number num(Object o) {
        if (o == null) return 0;
        if (o instanceof Number n) return n;
        try { return Double.parseDouble(o.toString()); } catch (Exception e) { return 0; }
    }

    private String formatDate(Object o) {
        if (o == null) return "—";
        try {
            if (o instanceof java.sql.Timestamp ts) return ts.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            if (o instanceof LocalDateTime ldt) return ldt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            return o.toString().substring(0, Math.min(16, o.toString().length()));
        } catch (Exception e) { return o.toString(); }
    }
}
