package com.eduvision.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class SessionCameraId implements Serializable {

    @Column(name = "session_id", columnDefinition = "char(36)")
    private String sessionId;

    @Column(name = "camera_id", columnDefinition = "char(36)")
    private String cameraId;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getCameraId() {
        return cameraId;
    }

    public void setCameraId(String cameraId) {
        this.cameraId = cameraId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SessionCameraId that)) {
            return false;
        }
        return Objects.equals(sessionId, that.sessionId)
                && Objects.equals(cameraId, that.cameraId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, cameraId);
    }
}
