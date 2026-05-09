package com.eduvision.service;

/**
 * SessionService — Lecture session lifecycle manager.
 *
 * <p>This service owns the full lifecycle of a {@link com.eduvision.model.LectureSession}:
 * creation, activation, monitoring, and completion.  It also acts as the trigger point
 * for downstream analytics — when a session ends, it fires off the R analytics pipeline
 * in the background so the student dashboard is populated without blocking the lecturer.
 *
 * <p>Key responsibilities:
 * <ul>
 *   <li><b>startSession()</b> — Creates the session entity, enforces the singleton guard
 *       (only one active session per course at a time), and broadcasts a WebSocket
 *       notification so enrolled students receive a push notification in the Flutter app.</li>
 *   <li><b>endSession()</b> — Marks the session as completed, clears the singleton registry,
 *       and asynchronously triggers {@link ReportService#computeStudentSummariesAsync} to
 *       run the R script that populates {@code student_lecture_summaries}.</li>
 * </ul>
 *
 * <p>The Python vision engine also calls this service indirectly: after the React frontend
 * POSTs to {@code /session/end} on this backend, this service signals the R pipeline.
 * The Python side is notified separately via its own {@code /session/end} endpoint.
 */

import com.eduvision.dto.session.SessionEndRequest;
import com.eduvision.dto.session.SessionStartRequest;
import com.eduvision.dto.session.SessionStatusDTO;
import com.eduvision.exception.ResourceNotFoundException;
import com.eduvision.model.AttendanceStatus;
import com.eduvision.model.Course;
import com.eduvision.model.LectureSession;
import com.eduvision.model.LectureSessionRegistry;
import com.eduvision.model.LectureSessionStatus;
import com.eduvision.model.User;
import com.eduvision.repository.SessionAttendanceRepository;
import com.eduvision.repository.SessionRegistryRepository;
import com.eduvision.repository.SessionRepository;
import com.eduvision.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class SessionService {

    private final SessionRepository sessionRepository;
    private final SessionAttendanceRepository attendanceRepository;
    private final SessionRegistryRepository registryRepository;
    private final UserRepository userRepository;
    private final SingletonGuardService singletonGuardService;
    private final EntityManager entityManager;
    private final ReportService reportService;
    private final WebSocketNotificationService wsNotificationService;

    public SessionService(
            SessionRepository sessionRepository,
            SessionAttendanceRepository attendanceRepository,
            SessionRegistryRepository registryRepository,
            UserRepository userRepository,
            SingletonGuardService singletonGuardService,
            EntityManager entityManager,
            ReportService reportService,
            WebSocketNotificationService wsNotificationService) {
        this.sessionRepository     = sessionRepository;
        this.attendanceRepository  = attendanceRepository;
        this.registryRepository    = registryRepository;
        this.userRepository        = userRepository;
        this.singletonGuardService = singletonGuardService;
        this.entityManager         = entityManager;
        this.reportService         = reportService;
        this.wsNotificationService = wsNotificationService;
    }

    /**
     * Creates and activates a new lecture session for the given course.
     *
     * <p>Steps performed:
     * <ol>
     *   <li>Looks up the {@link Course} and the currently authenticated lecturer.</li>
     *   <li>Builds a new {@link LectureSession} entity with status {@code active} and
     *       saves it to the database.</li>
     *   <li>Calls {@link SingletonGuardService#activateSession} to enforce the rule that
     *       only one session may be active per course at a time.  A second call for the
     *       same course will throw an exception.</li>
     *   <li>Broadcasts a {@code session_started} WebSocket event via
     *       {@link WebSocketNotificationService} so the Flutter student app can show
     *       a real-time "Your lecture has started" notification to enrolled students.</li>
     *   <li>Upserts the {@link LectureSessionRegistry} row that links the course to its
     *       currently active session (used for quick lookups without scanning the full
     *       sessions table).</li>
     * </ol>
     *
     * @param request contains courseId, scheduledStart/End, roomLocation, cameraType
     * @return a {@link SessionStatusDTO} snapshot of the newly created session
     */
    public SessionStatusDTO startSession(SessionStartRequest request) {
        Course course = entityManager.find(Course.class, request.getCourseId());
        if (course == null) {
            throw new ResourceNotFoundException("Course not found: " + request.getCourseId());
        }

        User lecturer = getCurrentUser();

        LectureSession session = new LectureSession();
        session.setId(UUID.randomUUID().toString());
        session.setCourse(course);
        session.setLecturer(lecturer);
        session.setScheduledStart(request.getScheduledStart());
        session.setScheduledEnd(request.getScheduledEnd());
        session.setActualStart(LocalDateTime.now());
        session.setStatus(LectureSessionStatus.active);
        session.setRoomLocation(request.getRoomLocation());
        session.setSessionMetadata("{\"cameraType\":\"" + request.getCameraType() + "\"}");
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());

        sessionRepository.save(session);
        singletonGuardService.activateSession(course.getId(), session.getId());

        // Notify enrolled students via WebSocket → Flutter picks this up and shows a notification
        String lecturerName = buildLecturerName(lecturer);
        wsNotificationService.sendSessionStarted(
                course.getId(), session.getId(),
                lecturerName, course.getTitle(),
                request.getRoomLocation());

        LectureSessionRegistry registry = registryRepository.findByCourse_Id(course.getId())
                .orElseGet(() -> {
                    LectureSessionRegistry item = new LectureSessionRegistry();
                    item.setId(UUID.randomUUID().toString());
                    item.setCourse(course);
                    return item;
                });
        registry.setActiveSession(session);
        registry.setLastActivatedAt(LocalDateTime.now());
        registryRepository.save(registry);

        return buildStatus(session);
    }

    /**
     * Marks a session as completed and auto-triggers R analytics in the background.
     *
     * <p>Steps performed:
     * <ol>
     *   <li>Sets {@code actualEnd}, changes status to {@code completed}, persists.</li>
     *   <li>Clears the {@link LectureSessionRegistry} active session pointer for the
     *       course so the singleton guard allows a new session to be started later.</li>
     *   <li>Calls {@link ReportService#computeStudentSummariesAsync} — this is a
     *       {@code @Async} method that runs the R script
     *       {@code compute_student_summaries.R} in a background thread pool.  The R
     *       script reads {@code student_emotion_snapshots} for this session, computes
     *       per-student analytics (concentration, emotion percentages, attentiveness),
     *       and upserts rows into {@code student_lecture_summaries}.  This table is
     *       what the student dashboard reads for all its KPIs.</li>
     * </ol>
     *
     * <p>Because step 3 is asynchronous, the lecturer's "End Session" HTTP response
     * is returned immediately — the analytics computation happens in the background
     * without blocking the UI.
     *
     * @param sessionId the UUID of the session to end
     * @param request   contains actualEnd timestamp
     * @return updated {@link SessionStatusDTO}
     */
    public SessionStatusDTO endSession(String sessionId, SessionEndRequest request) {
        LectureSession session = getSession(sessionId);

        session.setActualEnd(request.getActualEnd());
        session.setStatus(LectureSessionStatus.completed);
        session.setUpdatedAt(LocalDateTime.now());
        sessionRepository.save(session);

        registryRepository.findByCourse_Id(session.getCourse().getId())
                .filter(registry -> registry.getActiveSession() != null && sessionId.equals(registry.getActiveSession().getId()))
                .ifPresent(registry -> {
                    registry.setActiveSession(null);
                    registry.setLastDeactivatedAt(LocalDateTime.now());
                    registryRepository.save(registry);
                });

        // Automatically compute student analytics in the background.
        // Runs asynchronously so the lecturer's response is instant.
        reportService.computeStudentSummariesAsync(sessionId);

        return buildStatus(session);
    }

    @Transactional(readOnly = true)
    public SessionStatusDTO getSessionStatus(String sessionId) {
        LectureSession session = getSession(sessionId);
        return buildStatus(session);
    }

    @Transactional(readOnly = true)
    public List<SessionStatusDTO> getActiveSessions() {
        return sessionRepository.findByStatus(LectureSessionStatus.active)
                .stream()
                .map(this::buildStatus)
                .collect(Collectors.toList());
    }

    private LectureSession getSession(String sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Lecture session not found: " + sessionId));
    }

    /** Resolves the currently authenticated lecturer from the Spring Security context. */
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found: " + email));
    }

    private int countPresentStudents(String sessionId) {
        return attendanceRepository.countBySessionIdAndStatus(sessionId, AttendanceStatus.present);
    }

    private long calculateActiveTime(LectureSession session) {
        if (session.getActualStart() == null) {
            return 0L;
        }
        LocalDateTime end = session.getActualEnd() != null ? session.getActualEnd() : LocalDateTime.now();
        return Duration.between(session.getActualStart(), end).toMinutes();
    }

    private SessionStatusDTO buildStatus(LectureSession session) {
        SessionStatusDTO dto = new SessionStatusDTO();
        dto.setSessionId(session.getId());
        dto.setCourseName(session.getCourse() != null ? session.getCourse().getTitle() : null);
        dto.setLecturerName(buildLecturerName(session.getLecturer()));
        dto.setStatus(session.getStatus() != null ? session.getStatus().name() : null);
        dto.setStartTime(session.getActualStart() != null ? session.getActualStart() : session.getScheduledStart());
        dto.setStudentCount(countPresentStudents(session.getId()));
        dto.setActiveTime(calculateActiveTime(session));
        return dto;
    }

    private String buildLecturerName(User lecturer) {
        if (lecturer == null) {
            return null;
        }
        if (lecturer.getDisplayName() != null && !lecturer.getDisplayName().isBlank()) {
            return lecturer.getDisplayName();
        }
        return lecturer.getFirstName() + " " + lecturer.getLastName();
    }
}
