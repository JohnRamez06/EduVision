package com.eduvision.service;

import com.eduvision.dto.lecturer.SessionHistoryDTO;
import com.eduvision.exception.ResourceNotFoundException;
import com.eduvision.model.*;
import com.eduvision.repository.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class LecturerService {

    private final UserRepository userRepository;
    private final LecturerRepository lecturerRepository;
    private final CourseLecturerRepository courseLecturerRepository;
    private final SessionRepository sessionRepository;
    private final SessionAttendanceRepository attendanceRepository;
    private final AlertRepository alertRepository;
    private final StudentEmotionSnapshotRepository emotionSnapshotRepository;

    public LecturerService(
            UserRepository userRepository,
            LecturerRepository lecturerRepository,
            CourseLecturerRepository courseLecturerRepository,
            SessionRepository sessionRepository,
            SessionAttendanceRepository attendanceRepository,
            AlertRepository alertRepository,
            StudentEmotionSnapshotRepository emotionSnapshotRepository) {
        this.userRepository = userRepository;
        this.lecturerRepository = lecturerRepository;
        this.courseLecturerRepository = courseLecturerRepository;
        this.sessionRepository = sessionRepository;
        this.attendanceRepository = attendanceRepository;
        this.alertRepository = alertRepository;
        this.emotionSnapshotRepository = emotionSnapshotRepository;
    }

    /** Resolves the authenticated lecturer from the JWT principal (email). */
    public User getCurrentLecturer() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Authenticated lecturer not found: " + email));
    }

    public LecturerProfileDTO getProfile() {
        User user = getCurrentLecturer();
        Lecturer lecturer = user.getLecturer();

        LecturerProfileDTO dto = new LecturerProfileDTO();
        dto.setId(user.getId());
        dto.setFullName(user.getFirstName() + " " + user.getLastName());
        dto.setEmail(user.getEmail());
        if (lecturer != null) {
            dto.setEmployeeId(lecturer.getEmployeeId());
            dto.setDepartment(lecturer.getDepartment());
            dto.setSpecialization(lecturer.getSpecialization());
            dto.setOfficeLocation(lecturer.getOfficeLocation());
        }
        return dto;
    }

    public List<LecturerCourseDTO> getCourses() {
        User lecturer = getCurrentLecturer();

        return courseLecturerRepository
                .findByLecturerId(lecturer.getId())
                .stream()
                .map(cl -> {
                    Course c = cl.getCourse();
                    LecturerCourseDTO dto = new LecturerCourseDTO();
                    dto.setCourseId(c.getId());
                    dto.setCode(c.getCode());
                    dto.setTitle(c.getTitle());
                    dto.setDepartment(c.getDepartment());
                    dto.setSemester(c.getSemester());
                    dto.setAcademicYear(c.getAcademicYear());

                    // Count total sessions for this course
                    dto.setTotalSessions((int) c.getLectureSessions().size());

                    // Count active sessions
                    dto.setActiveSessions((int) c.getLectureSessions().stream()
                            .filter(s -> LectureSessionStatus.active.equals(s.getStatus()))
                            .count());

                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<SessionHistoryDTO> getSessionHistory() {
        User lecturer = getCurrentLecturer();

        return sessionRepository
                .findByLecturerIdOrderByScheduledStartDesc(lecturer.getId())
                .stream()
                .filter(s -> LectureSessionStatus.completed.equals(s.getStatus()))
                .map(this::buildSessionHistory)
                .collect(Collectors.toList());
    }

    public List<StudentSessionDTO> getSessionStudents(String sessionId) {
        // Verify lecturer owns this session
        LectureSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        if (!session.getLecturer().getId().equals(getCurrentLecturer().getId())) {
            throw new ResourceNotFoundException("Session not found: " + sessionId);
        }

        return attendanceRepository
                .findBySessionId(sessionId)
                .stream()
                .map(attendance -> {
                    User student = attendance.getStudent();
                    StudentSessionDTO dto = new StudentSessionDTO();
                    dto.setStudentId(student.getId());
                    dto.setStudentName(student.getFirstName() + " " + student.getLastName());
                    dto.setStatus(attendance.getStatus().name());
                    dto.setJoinedAt(attendance.getJoinedAt());
                    dto.setLeftAt(attendance.getLeftAt());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public String getActiveSessionId() {
        User lecturer = getCurrentLecturer();
        return sessionRepository.findByLecturerIdOrderByScheduledStartDesc(lecturer.getId())
                .stream()
                .filter(s -> LectureSessionStatus.active.equals(s.getStatus()))
                .findFirst()
                .map(LectureSession::getId)
                .orElse(null);
    }

    public List<DetectedStudentDTO> getDetectedStudents(String sessionId) {
        LectureSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));
        if (!session.getLecturer().getId().equals(getCurrentLecturer().getId())) {
            throw new ResourceNotFoundException("Session not found: " + sessionId);
        }

        List<StudentEmotionSnapshot> snapshots =
                emotionSnapshotRepository.findBySession_IdOrderByCapturedAtDesc(sessionId);

        // Keep only the most recent snapshot per student
        Map<String, StudentEmotionSnapshot> latestPerStudent = new LinkedHashMap<>();
        for (StudentEmotionSnapshot snap : snapshots) {
            String sid = snap.getStudent().getId();
            latestPerStudent.putIfAbsent(sid, snap);
        }

        return latestPerStudent.values().stream()
                .map(snap -> {
                    User student = snap.getStudent();
                    DetectedStudentDTO dto = new DetectedStudentDTO();
                    dto.setStudentId(student.getId());
                    dto.setStudentName(student.getFirstName() + " " + student.getLastName());
                    dto.setProfilePictureUrl(student.getProfilePictureUrl());
                    dto.setEmotion(snap.getEmotion() != null ? snap.getEmotion().name() : "neutral");
                    dto.setConcentration(snap.getConcentration() != null ? snap.getConcentration().name() : "medium");
                    dto.setConfidenceScore(snap.getConfidenceScore() != null ? snap.getConfidenceScore().doubleValue() : 0.0);
                    dto.setLastSeenAt(snap.getCapturedAt());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private SessionHistoryDTO buildSessionHistory(LectureSession session) {
        SessionHistoryDTO dto = new SessionHistoryDTO();
        dto.setSessionId(session.getId());
        dto.setCourseName(session.getCourse() != null ? session.getCourse().getTitle() : null);
        dto.setDate(session.getActualStart() != null ? session.getActualStart() : session.getScheduledStart());

        // Calculate average engagement (placeholder - would need real calculation)
        dto.setAvgEngagement(75.0); // Placeholder

        dto.setStudentCount(attendanceRepository.countBySessionIdAndStatus(session.getId(), AttendanceStatus.present));

        // Count alerts for this session
        dto.setAlertCount((int) alertRepository.findBySession_Id(session.getId()).size());

        return dto;
    }

    // Nested DTOs
    public static class LecturerProfileDTO {
        private String id;
        private String fullName;
        private String email;
        private String employeeId;
        private String department;
        private String specialization;
        private String officeLocation;

        // getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getEmployeeId() { return employeeId; }
        public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public String getSpecialization() { return specialization; }
        public void setSpecialization(String specialization) { this.specialization = specialization; }
        public String getOfficeLocation() { return officeLocation; }
        public void setOfficeLocation(String officeLocation) { this.officeLocation = officeLocation; }
    }

    public static class LecturerCourseDTO {
        private String courseId;
        private String code;
        private String title;
        private String department;
        private String semester;
        private String academicYear;
        private int totalSessions;
        private int activeSessions;

        // getters and setters
        public String getCourseId() { return courseId; }
        public void setCourseId(String courseId) { this.courseId = courseId; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public String getSemester() { return semester; }
        public void setSemester(String semester) { this.semester = semester; }
        public String getAcademicYear() { return academicYear; }
        public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
        public int getTotalSessions() { return totalSessions; }
        public void setTotalSessions(int totalSessions) { this.totalSessions = totalSessions; }
        public int getActiveSessions() { return activeSessions; }
        public void setActiveSessions(int activeSessions) { this.activeSessions = activeSessions; }
    }

    public static class StudentSessionDTO {
        private String studentId;
        private String studentName;
        private String status;
        private java.time.LocalDateTime joinedAt;
        private java.time.LocalDateTime leftAt;

        // getters and setters
        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }
        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public java.time.LocalDateTime getJoinedAt() { return joinedAt; }
        public void setJoinedAt(java.time.LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
        public java.time.LocalDateTime getLeftAt() { return leftAt; }
        public void setLeftAt(java.time.LocalDateTime leftAt) { this.leftAt = leftAt; }
    }

    public static class DetectedStudentDTO {
        private String studentId;
        private String studentName;
        private String profilePictureUrl;
        private String emotion;
        private String concentration;
        private double confidenceScore;
        private java.time.LocalDateTime lastSeenAt;

        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }
        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }
        public String getProfilePictureUrl() { return profilePictureUrl; }
        public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }
        public String getEmotion() { return emotion; }
        public void setEmotion(String emotion) { this.emotion = emotion; }
        public String getConcentration() { return concentration; }
        public void setConcentration(String concentration) { this.concentration = concentration; }
        public double getConfidenceScore() { return confidenceScore; }
        public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }
        public java.time.LocalDateTime getLastSeenAt() { return lastSeenAt; }
        public void setLastSeenAt(java.time.LocalDateTime lastSeenAt) { this.lastSeenAt = lastSeenAt; }
    }
}
