package com.eduvision.service;

/**
 * StudentService — Student-facing analytics and dashboard data provider.
 *
 * <p>This service is the primary read-path for the student Flutter app dashboard.
 * All data it returns originates from the {@code student_lecture_summaries} table,
 * which is populated by the R script {@code compute_student_summaries.R} after each
 * session ends.  The service itself never writes analytics — it only reads and
 * transforms what R has already computed.
 *
 * <p>Key data sources:
 * <ul>
 *   <li>{@code student_lecture_summaries} — per-session aggregated analytics
 *       (concentration, emotion percentages, attentiveness, dominant emotion).
 *       Written exclusively by the R pipeline via {@link ReportService}.</li>
 *   <li>{@code student_emotion_snapshots} — raw per-snapshot emotion data used
 *       to build the concentration timeline chart.</li>
 *   <li>{@code session_attendance} — join/leave records written by the Python
 *       vision engine via {@link AttendanceService}.</li>
 *   <li>{@code course_students} — enrollment records written at registration.</li>
 * </ul>
 *
 * <p>All methods are read-only transactions (the class-level annotation) and
 * resolve the calling student from the JWT principal via {@link #getCurrentUser()}.
 */

import com.eduvision.dto.student.*;
import com.eduvision.exception.ResourceNotFoundException;
import com.eduvision.model.*;
import com.eduvision.repository.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class StudentService {

    private final UserRepository                    userRepository;
    private final StudentLectureSummaryRepository   summaryRepository;
    private final StudentEmotionSnapshotRepository  emotionSnapshotRepository;
    private final CourseStudentRepository           courseStudentRepository;
    private final SessionAttendanceRepository       attendanceRepository;

    public StudentService(
            UserRepository userRepository,
            StudentLectureSummaryRepository summaryRepository,
            StudentEmotionSnapshotRepository emotionSnapshotRepository,
            CourseStudentRepository courseStudentRepository,
            SessionAttendanceRepository attendanceRepository) {
        this.userRepository           = userRepository;
        this.summaryRepository        = summaryRepository;
        this.emotionSnapshotRepository = emotionSnapshotRepository;
        this.courseStudentRepository  = courseStudentRepository;
        this.attendanceRepository     = attendanceRepository;
    }

    // ─── CURRENT USER ─────────────────────────────────────────────────────

    /**
     * Resolves the authenticated student from the JWT principal.
     *
     * <p>Spring Security places the authenticated principal's name (email) into
     * the {@link org.springframework.security.core.context.SecurityContext} after
     * the JWT filter validates the token.  This method reads that email and performs
     * a DB lookup via {@link UserRepository} to return the full {@link User} entity.
     *
     * <p>Throws {@link ResourceNotFoundException} if the email from the JWT does not
     * match any user in the database (e.g., account was deleted after token was issued).
     */
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Authenticated user not found: " + email));
    }

    // ─── PROFILE ──────────────────────────────────────────────────────────

    public StudentDashboardDTO.StudentProfileDTO getProfile() {
        User user    = getCurrentUser();
        Student stu  = user.getStudent();

        StudentDashboardDTO.StudentProfileDTO dto = new StudentDashboardDTO.StudentProfileDTO();
        dto.setId(user.getId());
        dto.setFullName(user.getFirstName() + " " + user.getLastName());
        dto.setEmail(user.getEmail());
        if (stu != null) {
            dto.setProgram(stu.getProgram());
            dto.setYearOfStudy(stu.getYearOfStudy());
            dto.setStudentNumber(stu.getStudentNumber());
        }
        return dto;
    }

    // ─── ENROLLED COURSES ─────────────────────────────────────────────────

    /**
     * Returns the list of courses the authenticated student is enrolled in,
     * along with attendance counts for each.
     *
     * <p>For each active enrollment (where {@code dropped_at IS NULL}):
     * <ul>
     *   <li>{@code totalSessions} — counts all {@code completed} sessions for that
     *       course by streaming the course's session collection in memory.</li>
     *   <li>{@code attendedSessions} — calls
     *       {@link SessionAttendanceRepository#countByStudent_IdAndSession_Course_IdAndStatus}
     *       which issues a single COUNT query against {@code session_attendance}
     *       filtered to {@code status = 'present'}.</li>
     * </ul>
     *
     * <p>These two counts power the "X / Y sessions attended" display on the
     * student dashboard's course cards.
     */
    public List<StudentDashboardDTO.EnrolledCourseDTO> getEnrolledCourses() {
        User user = getCurrentUser();

        return courseStudentRepository
                .findByStudent_IdAndDroppedAtIsNull(user.getId())
                .stream()
                .map(cs -> {
                    Course c = cs.getCourse();
                    StudentDashboardDTO.EnrolledCourseDTO dto =
                            new StudentDashboardDTO.EnrolledCourseDTO();
                    dto.setCourseId(c.getId());
                    dto.setCode(c.getCode());
                    dto.setTitle(c.getTitle());
                    dto.setDepartment(c.getDepartment());

                    // Count completed sessions for this course
                    int total = (int) c.getLectureSessions().stream()
                            .filter(s -> LectureSessionStatus.completed
                                    .equals(s.getStatus()))
                            .count();
                    dto.setTotalSessions(total);

                    // Count sessions where student was marked present
                    dto.setAttendedSessions(
                            attendanceRepository
                                    .countByStudent_IdAndSession_Course_IdAndStatus(
                                            user.getId(), c.getId(),
                                            AttendanceStatus.present));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // ─── LECTURE SUMMARIES ────────────────────────────────────────────────

    public List<LectureSummaryDTO> getLectureSummaries() {
        User user = getCurrentUser();
        return summaryRepository
                .findByStudent_IdOrderByGeneratedAtDesc(user.getId())
                .stream()
                .map(s -> enrichWithAttendance(LectureSummaryDTO.from(s),
                        user.getId(), s.getSession().getId()))
                .collect(Collectors.toList());
    }

    public List<LectureSummaryDTO> getCourseAnalytics(String courseId) {
        User user = getCurrentUser();
        return summaryRepository
                .findByStudent_IdAndCourse_IdOrderByGeneratedAtDesc(user.getId(), courseId)
                .stream()
                .map(s -> enrichWithAttendance(LectureSummaryDTO.from(s),
                        user.getId(), s.getSession().getId()))
                .collect(Collectors.toList());
    }

    public LectureSummaryDTO getLectureSummaryBySession(String sessionId) {
        User user = getCurrentUser();
        StudentLectureSummary summary = summaryRepository
                .findByStudent_IdAndSession_Id(user.getId(), sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No summary found for session: " + sessionId));
        return enrichWithAttendance(LectureSummaryDTO.from(summary),
                user.getId(), sessionId);
    }

    // ─── CONCENTRATION TIMELINE ───────────────────────────────────────────

    /**
     * Builds the per-snapshot concentration timeline for a single session.
     *
     * <p>Unlike the summary endpoints which read from {@code student_lecture_summaries},
     * this method reads directly from {@code student_emotion_snapshots} — the raw rows
     * written by the Python vision engine every 10 seconds during the session.
     *
     * <p>The returned {@link ConcentrationTimelineDTO} contains three parallel lists:
     * <ul>
     *   <li>{@code timestamps} — ISO-8601 strings of each snapshot's {@code captured_at}</li>
     *   <li>{@code concentrationScores} — numeric 0.0-1.0 values mapped from the
     *       {@link ConcentrationLevel} enum via {@link #concentrationToScore}</li>
     *   <li>{@code emotions} — raw emotion name strings for each snapshot</li>
     * </ul>
     *
     * <p>The Flutter chart widget plots concentrationScores over time to show how
     * the student's focus evolved during the lecture.
     */
    public ConcentrationTimelineDTO getConcentrationTimeline(String sessionId) {
        User user = getCurrentUser();

        List<StudentEmotionSnapshot> snapshots =
                emotionSnapshotRepository
                        .findByStudent_IdAndSession_IdOrderByCapturedAtAsc(
                                user.getId(), sessionId);

        if (snapshots.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No emotion data found for session: " + sessionId);
        }

        LectureSession session = snapshots.get(0).getSession();

        ConcentrationTimelineDTO dto = new ConcentrationTimelineDTO();
        dto.setSessionId(sessionId);
        dto.setSessionTitle(session.getTitle());
        dto.setCourseName(session.getCourse() != null
                ? session.getCourse().getTitle() : "");

        dto.setTimestamps(snapshots.stream()
                .map(s -> s.getCapturedAt().toString())
                .collect(Collectors.toList()));

        dto.setConcentrationScores(snapshots.stream()
                .map(s -> concentrationToScore(s.getConcentration()))
                .collect(Collectors.toList()));

        dto.setEmotions(snapshots.stream()
                .map(s -> s.getEmotion() != null ? s.getEmotion().name() : "neutral")
                .collect(Collectors.toList()));

        return dto;
    }

    // ─── OVERALL STATS ────────────────────────────────────────────────────

    /**
     * Aggregates all of a student's session summaries into a single set of KPIs
     * for the main dashboard overview panel.
     *
     * <p>All numeric values (avgConcentration, avgAttentiveness, emotion breakdown)
     * are computed as simple arithmetic means across all {@link StudentLectureSummary}
     * rows for the given student, using the {@link #avg} helper.
     *
     * <p>The {@code emotionBreakdown} map contains average percentages (0.0-1.0) for
     * each emotion label: happy, sad, angry, confused, neutral, engaged.  The
     * {@code mostFrequentEmotion} is derived as the key with the highest average.
     *
     * <p>{@code totalLecturesAttended} first tries the attendance table for an accurate
     * count; if no attendance rows exist (e.g., legacy data) it falls back to the
     * number of summary rows.
     *
     * @param userId the user ID of the student (not necessarily the caller)
     */
    public StudentDashboardDTO.OverallStatsDTO getOverallStats(String userId) {
        List<StudentLectureSummary> all =
                summaryRepository.findByStudent_IdOrderByGeneratedAtDesc(userId);

        StudentDashboardDTO.OverallStatsDTO stats =
                new StudentDashboardDTO.OverallStatsDTO();

        // Count sessions where the student was actually present or late
        long attended = attendanceRepository.countByStudent_IdAndStatusIn(
                userId, List.of(AttendanceStatus.present, AttendanceStatus.late));
        stats.setTotalLecturesAttended(attended > 0 ? attended : all.size());

        if (all.isEmpty()) return stats;

        stats.setAvgConcentration(
                avg(all, StudentLectureSummary::getAvgConcentration));
        stats.setAvgAttentiveness(
                avg(all, StudentLectureSummary::getAttentivePercentage));

        // Emotion breakdown map — average percentage per emotion across all sessions
        Map<String, Double> breakdown = new LinkedHashMap<>();
        breakdown.put("happy",    avg(all, StudentLectureSummary::getPctHappy));
        breakdown.put("sad",      avg(all, StudentLectureSummary::getPctSad));
        breakdown.put("angry",    avg(all, StudentLectureSummary::getPctAngry));
        breakdown.put("confused", avg(all, StudentLectureSummary::getPctConfused));
        breakdown.put("neutral",  avg(all, StudentLectureSummary::getPctNeutral));
        breakdown.put("engaged",  avg(all, StudentLectureSummary::getPctEngaged));
        stats.setEmotionBreakdown(breakdown);
        stats.setMostFrequentEmotion(
                breakdown.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse("neutral"));
        return stats;
    }

    // ─── PRIVATE HELPERS ──────────────────────────────────────────────────

    private LectureSummaryDTO enrichWithAttendance(LectureSummaryDTO dto,
                                                    String userId,
                                                    String sessionId) {
        attendanceRepository.findByStudent_IdAndSession_Id(userId, sessionId)
                .ifPresent(att -> dto.setAttendance(att.getStatus().name()));
        return dto;
    }

    /** Maps ConcentrationLevel enum → numeric score for the timeline chart. */
    private double concentrationToScore(ConcentrationLevel level) {
        if (level == null) return 0.5;
        return switch (level) {
            case high       -> 1.00;
            case medium     -> 0.67;
            case low        -> 0.33;
            case distracted -> 0.00;
        };
    }

    private double avg(List<StudentLectureSummary> list,
                       Function<StudentLectureSummary, BigDecimal> getter) {
        return list.stream()
                .filter(s -> getter.apply(s) != null)
                .mapToDouble(s -> getter.apply(s).doubleValue())
                .average().orElse(0.0);
    }
}
