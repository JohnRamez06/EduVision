package com.eduvision.service;

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

    /** Resolves the authenticated user from the JWT principal (email). */
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

        // Emotion breakdown map
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