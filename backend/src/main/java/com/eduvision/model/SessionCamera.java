package com.eduvision.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "session_cameras")
public class SessionCamera {

    @EmbeddedId
    private SessionCameraId id = new SessionCameraId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("sessionId")
    @JoinColumn(name = "session_id", nullable = false)
    private LectureSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("cameraId")
    @JoinColumn(name = "camera_id", nullable = false)
    private CameraConfiguration camera;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    public SessionCameraId getId() {
        return id;
    }

    public void setId(SessionCameraId id) {
        this.id = id;
    }

    public LectureSession getSession() {
        return session;
    }

    public void setSession(LectureSession session) {
        this.session = session;
    }

    public CameraConfiguration getCamera() {
        return camera;
    }

    public void setCamera(CameraConfiguration camera) {
        this.camera = camera;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
}
