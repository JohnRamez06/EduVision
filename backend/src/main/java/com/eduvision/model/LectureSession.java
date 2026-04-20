package com.eduvision.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "lecture_sessions")
public class LectureSession {

    @Id
    @Column(name = "id", columnDefinition = "char(36)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturer_id", nullable = false)
    private User lecturer;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "scheduled_start", nullable = false)
    private LocalDateTime scheduledStart;

    @Column(name = "scheduled_end", nullable = false)
    private LocalDateTime scheduledEnd;

    @Column(name = "actual_start")
    private LocalDateTime actualStart;

    @Column(name = "actual_end")
    private LocalDateTime actualEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LectureSessionStatus status;

    @Column(name = "room_location", length = 200)
    private String roomLocation;

    @Column(name = "session_metadata", columnDefinition = "json")
    private String sessionMetadata;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "activeSession")
    private Set<LectureSessionRegistry> activeInRegistries = new HashSet<>();

    @OneToMany(mappedBy = "session")
    private Set<SessionAttendance> sessionAttendances = new HashSet<>();

    @OneToMany(mappedBy = "session")
    private Set<SessionCamera> sessionCameras = new HashSet<>();

    @OneToMany(mappedBy = "session")
    private Set<EmotionSnapshot> emotionSnapshots = new HashSet<>();

    @OneToMany(mappedBy = "session")
    private Set<StudentEmotionSnapshot> studentEmotionSnapshots = new HashSet<>();

    @OneToMany(mappedBy = "session")
    private Set<StudentLectureSummary> studentLectureSummaries = new HashSet<>();

    @OneToMany(mappedBy = "session")
    private Set<Alert> alerts = new HashSet<>();

    @OneToMany(mappedBy = "session")
    private Set<Report> reports = new HashSet<>();

    @OneToMany(mappedBy = "session")
    private Set<AuditLog> auditLogs = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public User getLecturer() {
        return lecturer;
    }

    public void setLecturer(User lecturer) {
        this.lecturer = lecturer;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getScheduledStart() {
        return scheduledStart;
    }

    public void setScheduledStart(LocalDateTime scheduledStart) {
        this.scheduledStart = scheduledStart;
    }

    public LocalDateTime getScheduledEnd() {
        return scheduledEnd;
    }

    public void setScheduledEnd(LocalDateTime scheduledEnd) {
        this.scheduledEnd = scheduledEnd;
    }

    public LocalDateTime getActualStart() {
        return actualStart;
    }

    public void setActualStart(LocalDateTime actualStart) {
        this.actualStart = actualStart;
    }

    public LocalDateTime getActualEnd() {
        return actualEnd;
    }

    public void setActualEnd(LocalDateTime actualEnd) {
        this.actualEnd = actualEnd;
    }

    public LectureSessionStatus getStatus() {
        return status;
    }

    public void setStatus(LectureSessionStatus status) {
        this.status = status;
    }

    public String getRoomLocation() {
        return roomLocation;
    }

    public void setRoomLocation(String roomLocation) {
        this.roomLocation = roomLocation;
    }

    public String getSessionMetadata() {
        return sessionMetadata;
    }

    public void setSessionMetadata(String sessionMetadata) {
        this.sessionMetadata = sessionMetadata;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<LectureSessionRegistry> getActiveInRegistries() {
        return activeInRegistries;
    }

    public void setActiveInRegistries(Set<LectureSessionRegistry> activeInRegistries) {
        this.activeInRegistries = activeInRegistries;
    }

    public Set<SessionAttendance> getSessionAttendances() {
        return sessionAttendances;
    }

    public void setSessionAttendances(Set<SessionAttendance> sessionAttendances) {
        this.sessionAttendances = sessionAttendances;
    }

    public Set<SessionCamera> getSessionCameras() {
        return sessionCameras;
    }

    public void setSessionCameras(Set<SessionCamera> sessionCameras) {
        this.sessionCameras = sessionCameras;
    }

    public Set<EmotionSnapshot> getEmotionSnapshots() {
        return emotionSnapshots;
    }

    public void setEmotionSnapshots(Set<EmotionSnapshot> emotionSnapshots) {
        this.emotionSnapshots = emotionSnapshots;
    }

    public Set<StudentEmotionSnapshot> getStudentEmotionSnapshots() {
        return studentEmotionSnapshots;
    }

    public void setStudentEmotionSnapshots(Set<StudentEmotionSnapshot> studentEmotionSnapshots) {
        this.studentEmotionSnapshots = studentEmotionSnapshots;
    }

    public Set<StudentLectureSummary> getStudentLectureSummaries() {
        return studentLectureSummaries;
    }

    public void setStudentLectureSummaries(Set<StudentLectureSummary> studentLectureSummaries) {
        this.studentLectureSummaries = studentLectureSummaries;
    }

    public Set<Alert> getAlerts() {
        return alerts;
    }

    public void setAlerts(Set<Alert> alerts) {
        this.alerts = alerts;
    }

    public Set<Report> getReports() {
        return reports;
    }

    public void setReports(Set<Report> reports) {
        this.reports = reports;
    }

    public Set<AuditLog> getAuditLogs() {
        return auditLogs;
    }

    public void setAuditLogs(Set<AuditLog> auditLogs) {
        this.auditLogs = auditLogs;
    }
}
