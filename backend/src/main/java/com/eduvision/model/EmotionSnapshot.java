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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "emotion_snapshots")
public class EmotionSnapshot {

    @Id
    @Column(name = "id", columnDefinition = "char(36)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private LectureSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camera_id")
    private CameraConfiguration camera;

    @Column(name = "seq_index", nullable = false)
    private Long seqIndex;

    @Column(name = "captured_at", nullable = false)
    private LocalDateTime capturedAt;

    @Column(name = "frame_url")
    private String frameUrl;

    @Column(name = "student_count", nullable = false)
    private short studentCount;

    @Column(name = "avg_concentration", precision = 4, scale = 3)
    private BigDecimal avgConcentration;

    @Enumerated(EnumType.STRING)
    @Column(name = "dominant_emotion")
    private EmotionType dominantEmotion;

    @Column(name = "engagement_score", precision = 4, scale = 3)
    private BigDecimal engagementScore;

    @Column(name = "raw_payload", columnDefinition = "json")
    private String rawPayload;

    @Column(name = "processing_ms")
    private Integer processingMs;

    @OneToMany(mappedBy = "snapshot")
    private Set<StudentEmotionSnapshot> studentEmotionSnapshots = new HashSet<>();

    @OneToMany(mappedBy = "snapshot")
    private Set<Alert> alerts = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public Long getSeqIndex() {
        return seqIndex;
    }

    public void setSeqIndex(Long seqIndex) {
        this.seqIndex = seqIndex;
    }

    public LocalDateTime getCapturedAt() {
        return capturedAt;
    }

    public void setCapturedAt(LocalDateTime capturedAt) {
        this.capturedAt = capturedAt;
    }

    public String getFrameUrl() {
        return frameUrl;
    }

    public void setFrameUrl(String frameUrl) {
        this.frameUrl = frameUrl;
    }

    public short getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(short studentCount) {
        this.studentCount = studentCount;
    }

    public BigDecimal getAvgConcentration() {
        return avgConcentration;
    }

    public void setAvgConcentration(BigDecimal avgConcentration) {
        this.avgConcentration = avgConcentration;
    }

    public EmotionType getDominantEmotion() {
        return dominantEmotion;
    }

    public void setDominantEmotion(EmotionType dominantEmotion) {
        this.dominantEmotion = dominantEmotion;
    }

    public BigDecimal getEngagementScore() {
        return engagementScore;
    }

    public void setEngagementScore(BigDecimal engagementScore) {
        this.engagementScore = engagementScore;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public void setRawPayload(String rawPayload) {
        this.rawPayload = rawPayload;
    }

    public Integer getProcessingMs() {
        return processingMs;
    }

    public void setProcessingMs(Integer processingMs) {
        this.processingMs = processingMs;
    }

    public Set<StudentEmotionSnapshot> getStudentEmotionSnapshots() {
        return studentEmotionSnapshots;
    }

    public void setStudentEmotionSnapshots(Set<StudentEmotionSnapshot> studentEmotionSnapshots) {
        this.studentEmotionSnapshots = studentEmotionSnapshots;
    }

    public Set<Alert> getAlerts() {
        return alerts;
    }

    public void setAlerts(Set<Alert> alerts) {
        this.alerts = alerts;
    }
}
