package com.eduvision.service;

import com.eduvision.model.Report;
import com.eduvision.model.ReportStatus;
import com.eduvision.model.ReportType;
import com.eduvision.model.User;
import com.eduvision.model.WeeklyPeriods;
import com.eduvision.repository.ReportRepository;
import com.eduvision.repository.UserRepository;
import com.eduvision.repository.WeeklyPeriodsRepository;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
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

    public Report generateStudentWeeklyReport(String studentId, String weekId) {
        ensureWeeklyPeriod(weekId);
        String fileName = "student_" + studentId + "_week_" + weekId + ".pdf";
        String title    = "Student Weekly Report – Week " + weekId;
        return generate(ReportType.weekly_student, title,
                        "generators/generate_student_weekly.R",
                        new String[]{"student/", ""},
                        fileName, studentId, weekId);
    }

    public Report generateLecturerWeeklyReport(String lecturerId, String weekId) {
        ensureWeeklyPeriod(weekId);
        String fileName = "lecturer_" + lecturerId + "_week_" + weekId + ".pdf";
        String title    = "Lecturer Weekly Report – Week " + weekId;
        return generate(ReportType.weekly_lecturer, title,
                        "generators/generate_lecturer_weekly.R",
                        new String[]{"lecturer/", ""},
                        fileName, lecturerId, weekId);
    }

    public Report generateDeanWeeklyReport(String weekId) {
        ensureWeeklyPeriod(weekId);
        String fileName = "dean_week_" + weekId + ".pdf";
        String title    = "Dean Weekly Report – Week " + weekId;
        return generate(ReportType.weekly_dean, title,
                        "generators/generate_dean_weekly.R",
                        new String[]{"dean/", ""},
                        fileName, null, weekId);
    }

    public Report generateSessionReport(String sessionId) {
        String fileName = "session_" + sessionId + ".pdf";
        String title    = "Session Report";
        return generate(ReportType.session_summary, title,
                        "generators/generate_session_report.R",
                        new String[]{"session/", ""},
                        fileName, sessionId, null);
    }

    /**
     * Compute and persist student_lecture_summaries for all students in a session.
     * Must be called after a session ends so the analytics dashboard shows real data.
     */
    public boolean computeStudentSummaries(String sessionId) {
        boolean ok = rScriptExecutor.execute("scripts/compute_student_summaries.R", sessionId);
        if (!ok) {
            throw new RuntimeException("R summary computation failed for session: " + sessionId);
        }
        return true;
    }

    /**
     * Generate a per-student session PDF report for the currently authenticated student.
     */
    public Report generateMySessionReport(String sessionId) {
        User me = currentUser();
        return generateStudentSessionReport(me.getId(), sessionId);
    }

    /**
     * Generate a per-student session PDF report visible only to that student.
     */
    public Report generateStudentSessionReport(String studentId, String sessionId) {
        String fileName = "student_" + studentId + "_session_" + sessionId + ".pdf";
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
     * Ensures a weekly_periods row exists for the given academic week number.
     * Academic weeks start on Saturday; week 1 = SEMESTER_START.
     * If weekId is numeric, derives the Saturday–Friday date range from SEMESTER_START.
     * If weekId is a UUID it is assumed to be a direct PK — no auto-creation needed.
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
