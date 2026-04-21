package com.eduvision.dto.student;

import com.eduvision.model.StudentLectureSummary;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LectureSummaryDTO {

    private String        sessionId;
    private String        courseId;
    private String        courseName;
    private LocalDateTime date;               // actualStart, falls back to scheduledStart
    private String        attendance;         // AttendanceStatus name: present/late/absent/excused
    private BigDecimal    avgConcentration;   // 0.000 – 1.000
    private String        dominantEmotion;    // EmotionType name
    private BigDecimal    attentivePercentage;
    private BigDecimal    overallEngagement;

    // ── Static factory (maps entity → DTO) ───────────────────────────────
    public static LectureSummaryDTO from(StudentLectureSummary s) {
        LectureSummaryDTO dto = new LectureSummaryDTO();
        dto.sessionId           = s.getSession().getId();
        dto.courseId            = s.getCourse().getId();
        dto.courseName          = s.getCourse().getTitle();
        dto.date                = s.getSession().getActualStart() != null
                                    ? s.getSession().getActualStart()
                                    : s.getSession().getScheduledStart();
        dto.avgConcentration    = s.getAvgConcentration();
        dto.dominantEmotion     = s.getDominantEmotion() != null
                                    ? s.getDominantEmotion().name()
                                    : null;
        dto.attentivePercentage = s.getAttentivePercentage();
        dto.overallEngagement   = s.getOverallEngagement();
        // attendance is injected separately by StudentService
        return dto;
    }

    // Getters & Setters
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
    public String getAttendance() { return attendance; }
    public void setAttendance(String attendance) { this.attendance = attendance; }
    public BigDecimal getAvgConcentration() { return avgConcentration; }
    public void setAvgConcentration(BigDecimal avgConcentration) { this.avgConcentration = avgConcentration; }
    public String getDominantEmotion() { return dominantEmotion; }
    public void setDominantEmotion(String dominantEmotion) { this.dominantEmotion = dominantEmotion; }
    public BigDecimal getAttentivePercentage() { return attentivePercentage; }
    public void setAttentivePercentage(BigDecimal attentivePercentage) { this.attentivePercentage = attentivePercentage; }
    public BigDecimal getOverallEngagement() { return overallEngagement; }
    public void setOverallEngagement(BigDecimal overallEngagement) { this.overallEngagement = overallEngagement; }
}