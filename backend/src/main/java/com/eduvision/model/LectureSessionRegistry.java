package com.eduvision.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "lecture_session_registry")
public class LectureSessionRegistry {

    @Id
    @Column(name = "id", columnDefinition = "char(36)")
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false, unique = true)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "active_session_id")
    private LectureSession activeSession;

    @Column(name = "last_activated_at")
    private LocalDateTime lastActivatedAt;

    @Column(name = "last_deactivated_at")
    private LocalDateTime lastDeactivatedAt;

    @Column(name = "registry_metadata", columnDefinition = "json")
    private String registryMetadata;

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

    public LectureSession getActiveSession() {
        return activeSession;
    }

    public void setActiveSession(LectureSession activeSession) {
        this.activeSession = activeSession;
    }

    public LocalDateTime getLastActivatedAt() {
        return lastActivatedAt;
    }

    public void setLastActivatedAt(LocalDateTime lastActivatedAt) {
        this.lastActivatedAt = lastActivatedAt;
    }

    public LocalDateTime getLastDeactivatedAt() {
        return lastDeactivatedAt;
    }

    public void setLastDeactivatedAt(LocalDateTime lastDeactivatedAt) {
        this.lastDeactivatedAt = lastDeactivatedAt;
    }

    public String getRegistryMetadata() {
        return registryMetadata;
    }

    public void setRegistryMetadata(String registryMetadata) {
        this.registryMetadata = registryMetadata;
    }
}
