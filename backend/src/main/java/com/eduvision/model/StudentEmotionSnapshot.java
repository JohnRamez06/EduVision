package com.eduvision.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_emotion_snapshots")
public class StudentEmotionSnapshot {

    @Id
    @Column(name = "id", columnDefinition = "char(36)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "snapshot_id", nullable = false)
    private EmotionSnapshot snapshot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private LectureSession session;

    @Lob
    @Column(name = "face_embedding")
    private byte[] faceEmbedding;

    @Enumerated(EnumType.STRING)
    @Column(name = "emotion", nullable = false)
    private EmotionType emotion;

    @Enumerated(EnumType.STRING)
    @Column(name = "concentration", nullable = false)
    private ConcentrationLevel concentration;

    @Column(name = "confidence_score", nullable = false, precision = 4, scale = 3)
    private BigDecimal confidenceScore;

    @Column(name = "bounding_box", columnDefinition = "json")
    private String boundingBox;

    @Column(name = "gaze_direction", columnDefinition = "json")
    private String gazeDirection;

    @Column(name = "captured_at", nullable = false)
    private LocalDateTime capturedAt;

    @Column(name = "is_anonymised", nullable = false)
    private boolean anonymised;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public EmotionSnapshot getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(EmotionSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public LectureSession getSession() {
        return session;
    }

    public void setSession(LectureSession session) {
        this.session = session;
    }

    public byte[] getFaceEmbedding() {
        return faceEmbedding;
    }

    public void setFaceEmbedding(byte[] faceEmbedding) {
        this.faceEmbedding = faceEmbedding;
    }

    public EmotionType getEmotion() {
        return emotion;
    }

    public void setEmotion(EmotionType emotion) {
        this.emotion = emotion;
    }

    public ConcentrationLevel getConcentration() {
        return concentration;
    }

    public void setConcentration(ConcentrationLevel concentration) {
        this.concentration = concentration;
    }

    public BigDecimal getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(BigDecimal confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(String boundingBox) {
        this.boundingBox = boundingBox;
    }

    public String getGazeDirection() {
        return gazeDirection;
    }

    public void setGazeDirection(String gazeDirection) {
        this.gazeDirection = gazeDirection;
    }

    public LocalDateTime getCapturedAt() {
        return capturedAt;
    }

    public void setCapturedAt(LocalDateTime capturedAt) {
        this.capturedAt = capturedAt;
    }

    public boolean isAnonymised() {
        return anonymised;
    }

    public void setAnonymised(boolean anonymised) {
        this.anonymised = anonymised;
    }
}
