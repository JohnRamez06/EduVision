package com.eduvision.service;

/**
 * ReportService — R analytics pipeline trigger and report file manager.
 *
 * <p>This service bridges the Java backend and the R analytics layer.  It has
 * two distinct roles:
 *
 * <ol>
 *   <li><b>Analytics computation</b> — after a session ends,
 *       {@link #computeStudentSummariesAsync} fires the R script
 *       {@code compute_student_summaries.R} which reads raw
 *       {@code student_emotion_snapshots} and populates
 *       {@code student_lecture_summaries}.  This is the pipeline that makes
 *       per-student analytics available on the student dashboard.</li>
 *
 *   <li><b>HTML report generation</b> — on-demand methods (weekly student,
 *       weekly lecturer, dean, session, per-student session) invoke R Markdown
 *       generator scripts that produce HTML files stored under
 *       {@code analytics-r/output/}.  Report status (generating / ready / failed)
 *       is tracked in the {@code reports} table so the frontend can poll for
 *       completion.</li>
 * </ol>
 *
 * <p>All R invocations go through {@link RScriptExecutor#execute} which spawns
 * a child process via {@link ProcessBuilder} and streams stdout/stderr to the
 * Spring Boot log.
 */

import com.eduvision.model.Report;
import com.eduvision.model.ReportStatus;
import com.eduvision.model.ReportType;
import com.eduvision.model.User;
import com.eduvision.model.WeeklyPeriods;
import com.eduvision.repository.ReportRepository;
import com.eduvision.repository.UserRepository;
import com.eduvision.repository.WeeklyPeriodsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    // Root output directory where R scripts write their HTML files.
    // Resolved relative to the Spring Boot working directory's parent
    // (i.e., the eduvision/ monorepo root), then into analytics-r/output/.
    private static final String OUTPUT_BASE =
        Paths.get(System.getProperty("user.dir")).getParent().resolve("analytics-r/output").toString();

    /** Academic semester start: Saturday 14 Feb 2026. Weeks run Sat–Fri. */
    private static final LocalDate SEMESTER_START = LocalDate.of(2026, 2, 14);

    private final RScriptExecutor rScriptExecutor;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final WeeklyPeriodsRepository weeklyPeriodsRepository;

    public ReportService(RScriptExecutor rScriptExecutor,
                         ReportRepository reportRepository,
                         UserRepository userRepository,
                         WeeklyPeriodsRepository weeklyPeriodsRepository) {
        this.rScriptExecutor = rScriptExecutor;
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.weeklyPeriodsRepository = weeklyPeriodsRepository;
    }

    // ── public API ────────────────────────────────────────────────────────────

    // HTML output — no LaTeX/TinyTeX needed (cross-platform Windows + Mac)
    public Report generateStudentWeeklyReport(String studentId, String weekId) {
        ensureWeeklyPeriod(weekId);
        String fileName = "student_" + studentId + "_week_" + weekId + ".html";
        String title    = "Student Weekly Report – Week " + weekId;
        return generate(ReportType.weekly_student, title,
                        "generators/generate_student_weekly.R",
                        new String[]{"student/", ""},
                        fileName, studentId, weekId);
    }

    public Report generateLecturerWeeklyReport(String lecturerId, String weekId) {
        ensureWeeklyPeriod(weekId);
        String fileName = "lecturer_" + lecturerId + "_week_" + weekId + ".html";
        String title    = "Lecturer Weekly Report – Week " + weekId;
        return generate(ReportType.weekly_lecturer, title,
                        "generators/generate_lecturer_weekly.R",
                        new String[]{"lecturer/", ""},
                        fileName, lecturerId, weekId);
    }

    public Report generateDeanWeeklyReport(String weekId) {
        ensureWeeklyPeriod(weekId);
        String fileName = "dean_week_" + weekId + ".html";
        String title    = "Dean Weekly Report – Week " + weekId;
        return generate(ReportType.weekly_dean, title,
                        "generators/generate_dean_weekly.R",
                        new String[]{"dean/", ""},
                        fileName, null, weekId);
    }

    public Report generateSessionReport(String sessionId) {
        String fileName = "session_" + sessionId + ".html";
        String title    = "Session Report";
        return generate(ReportType.session_summary, title,
                        "generators/generate_session_report.R",
                        new String[]{"session/", ""},
                        fileName, sessionId, null);
    }

    /**
     * Synchronously computes and persists {@code student_lecture_summaries} for
     * every student who has emotion snapshots in the given session.
     *
     * <p>Invokes {@code analytics-r/scripts/compute_student_summaries.R} via
     * {@link RScriptExecutor#execute}, passing the session UUID as a command-line
     * argument.  The R script reads {@code student_emotion_snapshots}, computes
     * emotion percentages, concentration scores, attentiveness, and dominant emotion,
     * then upserts one row per student into {@code student_lecture_summaries}.
     *
     * <p>Throws {@link RuntimeException} if the R process exits with a non-zero
     * code (e.g., database connection failure or missing snapshots).
     *
     * @param sessionId UUID of the completed session
     * @return true on success
     */
    public boolean computeStudentSummaries(String sessionId) {
        boolean ok = rScriptExecutor.execute("scripts/compute_student_summaries.R", sessionId);
        if (!ok) {
            throw new RuntimeException("R summary computation failed for session: " + sessionId);
        }
        return true;
    }

    /**
     * Fire-and-forget analytics trigger — called automatically when a session ends.
     *
     * <p>This method is annotated with {@code @Async} so Spring runs it in a
     * background thread pool (configured via {@code @EnableAsync} in the application).
     * This means {@link SessionService#endSession} can return an HTTP response to the
     * lecturer immediately while R does its work in the background.
     *
     * <p>The background thread calls {@link RScriptExecutor#execute} with
     * {@code compute_student_summaries.R} and logs success or failure.  Any exception
     * is caught and logged — it does NOT propagate back to the HTTP request thread.
     *
     * <p>After this method completes (typically in a few seconds), the
     * {@code student_lecture_summaries} table will have fresh rows that the student
     * dashboard can immediately read.
     *
     * @param sessionId UUID of the session that just ended
     */
    @Async
    public void computeStudentSummariesAsync(String sessionId) {
        log.info("[ReportService] Auto-computing student summaries for session {}", sessionId);
        try {
            boolean ok = rScriptExecutor.execute("scripts/compute_student_summaries.R", sessionId);
            if (ok) {
                log.info("[ReportService] Student summaries computed successfully for session {}", sessionId);
            } else {
                log.warn("[ReportService] R script returned non-zero exit for session {}", sessionId);
            }
        } catch (Exception e) {
            log.error("[ReportService] Failed to compute summaries for session {}: {}", sessionId, e.getMessage());
        }
    }

    /**
     * Generates a per-student HTML session report for the currently authenticated student.
     *
     * <p>Delegates to {@link #generateStudentSessionReport} with the caller's user ID.
     * The returned {@link Report} entity contains the file URL and status; the actual
     * HTML file is produced by the R script
     * {@code generators/generate_student_session_report.R} and saved under
     * {@code analytics-r/output/student/}.
     *
     * <p>This report shows the student their own emotion timeline, concentration
     * breakdown, and attendance detail for a single session.
     */
    public Report generateMySessionReport(String sessionId) {
        User me = currentUser();
        return generateStudentSessionReport(me.getId(), sessionId);
    }

    /**
     * Generates a per-student HTML session report for an explicit studentId.
     *
     * <p>Calls the R script {@code generators/generate_student_session_report.R}
     * with arguments [studentId, sessionId].  The script queries the DB, renders
     * an R Markdown document, and writes the output HTML to
     * {@code analytics-r/output/student/student_<id>_session_<id>.html}.
     *
     * <p>The report status (generating → ready or failed) is persisted in the
     * {@code reports} table so the API can serve polling clients.
     *
     * @param studentId UUID of the student
     * @param sessionId UUID of the session
     */
    public Report generateStudentSessionReport(String studentId, String sessionId) {
        String fileName = "student_" + studentId + "_session_" + sessionId + ".html";
        String title    = "My Session Report";
        return generate(ReportType.session_summary, title,
                        "generators/generate_student_session_report.R",
                        new String[]{"student/", ""},
                        fileName, studentId, sessionId);
    }

    public List<Report> getMyReports() {
        User me = currentUser();
        return reportRepository.findByRequestedByIdOrderByRequestedAtDesc(me.getId());
    }

    public Resource getReportFile(String fileName) {
        for (String sub : new String[]{"student/", "session/", "lecturer/", "dean/", ""}) {
            Path path = Paths.get(OUTPUT_BASE, sub + fileName);
            if (path.toFile().exists()) {
                return new FileSystemResource(path.toFile());
            }
        }
        throw new RuntimeException("Report file not found: " + fileName);
    }

    // ── internals ─────────────────────────────────────────────────────────────

    private Report generate(ReportType type, String title, String script,
                             String[] subdirs, String fileName,
                             String subjectId, String weekId) {
        User requester = currentUser();

        // Create a report record in status "generating" so the frontend can poll
        Report report = new Report();
        report.setId(UUID.randomUUID().toString());
        report.setType(type);
        report.setStatus(ReportStatus.generating);
        report.setTitle(title);
        report.setRScriptUsed(script);
        report.setRequestedBy(requester);
        report.setRequestedAt(LocalDateTime.now());
        reportRepository.save(report);

        try {
            String[] args = weekId != null && subjectId != null
                ? new String[]{subjectId, weekId}
                : weekId != null
                    ? new String[]{weekId}
                    : new String[]{subjectId};

            boolean ok = rScriptExecutor.execute(script, args);

            if (ok) {
                Path filePath = resolveFile(subdirs, fileName);
                report.setStatus(ReportStatus.ready);
                report.setFileUrl("/api/v1/reports/download/" + fileName);
                if (filePath != null) {
                    report.setFileSizeBytes(filePath.toFile().length());
                }
            } else {
                report.setStatus(ReportStatus.failed);
            }
        } catch (Exception e) {
            report.setStatus(ReportStatus.failed);
        }

        report.setCompletedAt(LocalDateTime.now());
        return reportRepository.save(report);
    }

    private Path resolveFile(String[] subdirs, String fileName) {
        for (String sub : subdirs) {
            Path p = Paths.get(OUTPUT_BASE, sub + fileName);
            if (p.toFile().exists()) return p;
        }
        return null;
    }

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }

    /**
     * Ensures a {@code weekly_periods} row exists for the given academic week number.
     *
     * <p>Academic weeks in EduVision run Saturday–Friday starting from
     * {@code SEMESTER_START} (14 Feb 2026).  If the client passes a plain integer
     * week number (e.g., "3"), this method computes the corresponding
     * Saturday–Friday date range and inserts the row if it doesn't exist.
     *
     * <p>If {@code weekId} is a UUID (already a direct PK reference), no action is
     * taken because the row is assumed to exist already.
     *
     * @param weekId either a numeric week number string or a UUID primary key
     */
    private void ensureWeeklyPeriod(String weekId) {
        if (weekId == null || !weekId.matches("\\d+")) return;

        int weekNumber = Integer.parseInt(weekId);
        int year       = SEMESTER_START.getYear();

        if (weeklyPeriodsRepository.findTopByWeekNumberAndYear(weekNumber, year).isPresent()) {
            return; // already exists
        }

        // Academic week: semester_start + (weekNumber-1)*7 days → that Saturday
        LocalDate weekStart = SEMESTER_START.plusDays((long)(weekNumber - 1) * 7);
        LocalDate weekEnd   = weekStart.plusDays(6);

        WeeklyPeriods wp = new WeeklyPeriods();
        wp.setId(UUID.randomUUID().toString());
        wp.setWeekNumber(weekNumber);
        wp.setYear(year);
        wp.setStartDate(weekStart);
        wp.setEndDate(weekEnd);
        weeklyPeriodsRepository.save(wp);
    }
}
