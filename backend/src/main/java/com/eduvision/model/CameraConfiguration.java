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
@Table(name = "camera_configurations")
public class CameraConfiguration {

    @Id
    @Column(name = "id", columnDefinition = "char(36)")
    private String id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "factory_type", nullable = false)
    private CameraFactoryType factoryType;

    @Column(name = "device_index")
    private Short deviceIndex;

    @Column(name = "stream_url")
    private String streamUrl;

    @Column(name = "rtsp_username", length = 100)
    private String rtspUsername;

    @Column(name = "rtsp_password_enc")
    private String rtspPasswordEnc;

    @Column(name = "device_token")
    private String deviceToken;

    @Column(name = "resolution_w", nullable = false)
    private short resolutionW;

    @Column(name = "resolution_h", nullable = false)
    private short resolutionH;

    @Column(name = "fps", nullable = false)
    private byte fps;

    @Column(name = "extra_config", columnDefinition = "json")
    private String extraConfig;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "camera")
    private Set<SessionCamera> sessionCameras = new HashSet<>();

    @OneToMany(mappedBy = "camera")
    private Set<EmotionSnapshot> emotionSnapshots = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CameraFactoryType getFactoryType() {
        return factoryType;
    }

    public void setFactoryType(CameraFactoryType factoryType) {
        this.factoryType = factoryType;
    }

    public Short getDeviceIndex() {
        return deviceIndex;
    }

    public void setDeviceIndex(Short deviceIndex) {
        this.deviceIndex = deviceIndex;
    }

    public String getStreamUrl() {
        return streamUrl;
    }

    public void setStreamUrl(String streamUrl) {
        this.streamUrl = streamUrl;
    }

    public String getRtspUsername() {
        return rtspUsername;
    }

    public void setRtspUsername(String rtspUsername) {
        this.rtspUsername = rtspUsername;
    }

    public String getRtspPasswordEnc() {
        return rtspPasswordEnc;
    }

    public void setRtspPasswordEnc(String rtspPasswordEnc) {
        this.rtspPasswordEnc = rtspPasswordEnc;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public short getResolutionW() {
        return resolutionW;
    }

    public void setResolutionW(short resolutionW) {
        this.resolutionW = resolutionW;
    }

    public short getResolutionH() {
        return resolutionH;
    }

    public void setResolutionH(short resolutionH) {
        this.resolutionH = resolutionH;
    }

    public byte getFps() {
        return fps;
    }

    public void setFps(byte fps) {
        this.fps = fps;
    }

    public String getExtraConfig() {
        return extraConfig;
    }

    public void setExtraConfig(String extraConfig) {
        this.extraConfig = extraConfig;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
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
}
