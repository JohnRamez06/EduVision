package com.eduvision.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_lecture_summaries")
public class StudentLectureSummary {

    @Id
    @Column(name = "id", columnDefinition = "char(36)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private LectureSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "pct_happy", precision = 4, scale = 3)
    private BigDecimal pctHappy;

    @Column(name = "pct_sad", precision = 4, scale = 3)
    private BigDecimal pctSad;

    @Column(name = "pct_angry", precision = 4, scale = 3)
    private BigDecimal pctAngry;

    @Column(name = "pct_confused", precision = 4, scale = 3)
    private BigDecimal pctConfused;

    @Column(name = "pct_neutral", precision = 4, scale = 3)
    private BigDecimal pctNeutral;

    @Column(name = "pct_engaged", precision = 4, scale = 3)
    private BigDecimal pctEngaged;

    @Column(name = "pct_high_conc", precision = 4, scale = 3)
    private BigDecimal pctHighConc;

    @Column(name = "pct_med_conc", precision = 4, scale = 3)
    private BigDecimal pctMedConc;

    @Column(name = "pct_low_conc", precision = 4, scale = 3)
    private BigDecimal pctLowConc;

    @Column(name = "pct_distracted", precision = 4, scale = 3)
    private BigDecimal pctDistracted;

    @Column(name = "overall_engagement", precision = 4, scale = 3)
    private BigDecimal overallEngagement;

    @Column(name = "attention_score", precision = 4, scale = 3)
    private BigDecimal attentionScore;

    @Column(name = "participation_score", precision = 4, scale = 3)
    private BigDecimal participationScore;

    @Column(name = "r_analysis_json", columnDefinition = "json")
    private String rAnalysisJson;

    @Column(name = "snapshot_count", nullable = false)
    private int snapshotCount;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public BigDecimal getPctHappy() {
        return pctHappy;
    }

    public void setPctHappy(BigDecimal pctHappy) {
        this.pctHappy = pctHappy;
    }

    public BigDecimal getPctSad() {
        return pctSad;
    }

    public void setPctSad(BigDecimal pctSad) {
        this.pctSad = pctSad;
    }

    public BigDecimal getPctAngry() {
        return pctAngry;
    }

    public void setPctAngry(BigDecimal pctAngry) {
        this.pctAngry = pctAngry;
    }

    public BigDecimal getPctConfused() {
        return pctConfused;
    }

    public void setPctConfused(BigDecimal pctConfused) {
        this.pctConfused = pctConfused;
    }

    public BigDecimal getPctNeutral() {
        return pctNeutral;
    }

    public void setPctNeutral(BigDecimal pctNeutral) {
        this.pctNeutral = pctNeutral;
    }

    public BigDecimal getPctEngaged() {
        return pctEngaged;
    }

    public void setPctEngaged(BigDecimal pctEngaged) {
        this.pctEngaged = pctEngaged;
    }

    public BigDecimal getPctHighConc() {
        return pctHighConc;
    }

    public void setPctHighConc(BigDecimal pctHighConc) {
        this.pctHighConc = pctHighConc;
    }

    public BigDecimal getPctMedConc() {
        return pctMedConc;
    }

    public void setPctMedConc(BigDecimal pctMedConc) {
        this.pctMedConc = pctMedConc;
    }

    public BigDecimal getPctLowConc() {
        return pctLowConc;
    }

    public void setPctLowConc(BigDecimal pctLowConc) {
        this.pctLowConc = pctLowConc;
    }

    public BigDecimal getPctDistracted() {
        return pctDistracted;
    }

    public void setPctDistracted(BigDecimal pctDistracted) {
        this.pctDistracted = pctDistracted;
    }

    public BigDecimal getOverallEngagement() {
        return overallEngagement;
    }

    public void setOverallEngagement(BigDecimal overallEngagement) {
        this.overallEngagement = overallEngagement;
    }

    public BigDecimal getAttentionScore() {
        return attentionScore;
    }

    public void setAttentionScore(BigDecimal attentionScore) {
        this.attentionScore = attentionScore;
    }

    public BigDecimal getParticipationScore() {
        return participationScore;
    }

    public void setParticipationScore(BigDecimal participationScore) {
        this.participationScore = participationScore;
    }

    public String getRAnalysisJson() {
        return rAnalysisJson;
    }

    public void setRAnalysisJson(String rAnalysisJson) {
        this.rAnalysisJson = rAnalysisJson;
    }

    public int getSnapshotCount() {
        return snapshotCount;
    }

    public void setSnapshotCount(int snapshotCount) {
        this.snapshotCount = snapshotCount;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
