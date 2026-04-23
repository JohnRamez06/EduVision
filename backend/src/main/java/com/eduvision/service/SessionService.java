package com.eduvision.service;

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

    public SessionService(
            SessionRepository sessionRepository,
            SessionAttendanceRepository attendanceRepository,
            SessionRegistryRepository registryRepository,
            UserRepository userRepository,
            SingletonGuardService singletonGuardService,
            EntityManager entityManager) {
        this.sessionRepository = sessionRepository;
        this.attendanceRepository = attendanceRepository;
        this.registryRepository = registryRepository;
        this.userRepository = userRepository;
        this.singletonGuardService = singletonGuardService;
        this.entityManager = entityManager;
    }

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
