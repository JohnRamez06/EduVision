package com.eduvision.model;

import jakarta.persistence.*;
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
    private User student;                       // User.id = Student.userId (MapsId)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private LectureSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    // ── Emotion percentages ───────────────────────────────────────────────
    @Column(name = "pct_happy",    precision = 4, scale = 3) private BigDecimal pctHappy;
    @Column(name = "pct_sad",      precision = 4, scale = 3) private BigDecimal pctSad;
    @Column(name = "pct_angry",    precision = 4, scale = 3) private BigDecimal pctAngry;
    @Column(name = "pct_confused", precision = 4, scale = 3) private BigDecimal pctConfused;
    @Column(name = "pct_neutral",  precision = 4, scale = 3) private BigDecimal pctNeutral;
    @Column(name = "pct_engaged",  precision = 4, scale = 3) private BigDecimal pctEngaged;

    // ── Concentration percentages ─────────────────────────────────────────
    @Column(name = "pct_high_conc",  precision = 4, scale = 3) private BigDecimal pctHighConc;
    @Column(name = "pct_med_conc",   precision = 4, scale = 3) private BigDecimal pctMedConc;
    @Column(name = "pct_low_conc",   precision = 4, scale = 3) private BigDecimal pctLowConc;
    @Column(name = "pct_distracted", precision = 4, scale = 3) private BigDecimal pctDistracted;

    // ── Analysis scores ───────────────────────────────────────────────────
    @Column(name = "overall_engagement",  precision = 4, scale = 3) private BigDecimal overallEngagement;
    @Column(name = "attention_score",     precision = 4, scale = 3) private BigDecimal attentionScore;
    @Column(name = "participation_score", precision = 4, scale = 3) private BigDecimal participationScore;

    // ── NEW fields ✦ ──────────────────────────────────────────────────────
    @Column(name = "avg_concentration",   precision = 4, scale = 3)
    private BigDecimal avgConcentration;           // weighted mean across snapshots

    @Column(name = "attentive_percentage", precision = 4, scale = 3)
    private BigDecimal attentivePercentage;        // pctHighConc + pctMedConc stored for fast reads

    @Enumerated(EnumType.STRING)
    @Column(name = "dominant_emotion")
    private EmotionType dominantEmotion;           // EmotionType enum: happy/sad/angry/confused/neutral/engaged…

    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations;                // JSON array of suggestion strings

    // ── JSON / meta ───────────────────────────────────────────────────────
    @Column(name = "r_analysis_json", columnDefinition = "JSON")
    private String rAnalysisJson;

    @Column(name = "snapshot_count", nullable = false)
    private int snapshotCount;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ── Getters & Setters ─────────────────────────────────────────────────
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public LectureSession getSession() { return session; }
    public void setSession(LectureSession session) { this.session = session; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public BigDecimal getPctHappy() { return pctHappy; }
    public void setPctHappy(BigDecimal v) { this.pctHappy = v; }

    public BigDecimal getPctSad() { return pctSad; }
    public void setPctSad(BigDecimal v) { this.pctSad = v; }

    public BigDecimal getPctAngry() { return pctAngry; }
    public void setPctAngry(BigDecimal v) { this.pctAngry = v; }

    public BigDecimal getPctConfused() { return pctConfused; }
    public void setPctConfused(BigDecimal v) { this.pctConfused = v; }

    public BigDecimal getPctNeutral() { return pctNeutral; }
    public void setPctNeutral(BigDecimal v) { this.pctNeutral = v; }

    public BigDecimal getPctEngaged() { return pctEngaged; }
    public void setPctEngaged(BigDecimal v) { this.pctEngaged = v; }

    public BigDecimal getPctHighConc() { return pctHighConc; }
    public void setPctHighConc(BigDecimal v) { this.pctHighConc = v; }

    public BigDecimal getPctMedConc() { return pctMedConc; }
    public void setPctMedConc(BigDecimal v) { this.pctMedConc = v; }

    public BigDecimal getPctLowConc() { return pctLowConc; }
    public void setPctLowConc(BigDecimal v) { this.pctLowConc = v; }

    public BigDecimal getPctDistracted() { return pctDistracted; }
    public void setPctDistracted(BigDecimal v) { this.pctDistracted = v; }

    public BigDecimal getOverallEngagement() { return overallEngagement; }
    public void setOverallEngagement(BigDecimal v) { this.overallEngagement = v; }

    public BigDecimal getAttentionScore() { return attentionScore; }
    public void setAttentionScore(BigDecimal v) { this.attentionScore = v; }

    public BigDecimal getParticipationScore() { return participationScore; }
    public void setParticipationScore(BigDecimal v) { this.participationScore = v; }

    public BigDecimal getAvgConcentration() { return avgConcentration; }
    public void setAvgConcentration(BigDecimal v) { this.avgConcentration = v; }

    public BigDecimal getAttentivePercentage() { return attentivePercentage; }
    public void setAttentivePercentage(BigDecimal v) { this.attentivePercentage = v; }

    public EmotionType getDominantEmotion() { return dominantEmotion; }
    public void setDominantEmotion(EmotionType dominantEmotion) { this.dominantEmotion = dominantEmotion; }

    public String getRecommendations() { return recommendations; }
    public void setRecommendations(String recommendations) { this.recommendations = recommendations; }

    public String getRAnalysisJson() { return rAnalysisJson; }
    public void setRAnalysisJson(String rAnalysisJson) { this.rAnalysisJson = rAnalysisJson; }

    public int getSnapshotCount() { return snapshotCount; }
    public void setSnapshotCount(int snapshotCount) { this.snapshotCount = snapshotCount; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}