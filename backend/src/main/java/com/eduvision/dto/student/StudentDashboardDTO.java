package com.eduvision.dto.student;

import java.util.List;
import java.util.Map;

public class StudentDashboardDTO {

    private StudentProfileDTO              studentInfo;
    private List<EnrolledCourseDTO>        enrolledCourses;
    private List<LectureSummaryDTO>        recentSummaries;   // capped at 5 by facade
    private OverallStatsDTO                overallStats;
    private List<RecommendationDTO>        recommendations;

    // ── Nested: profile ──────────────────────────────────────────────────
    public static class StudentProfileDTO {
        private String id;
        private String fullName;        // firstName + " " + lastName from User
        private String email;
        private String program;         // Student.program
        private Byte   yearOfStudy;     // Student.yearOfStudy
        private String studentNumber;   // Student.studentNumber

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getProgram() { return program; }
        public void setProgram(String program) { this.program = program; }
        public Byte getYearOfStudy() { return yearOfStudy; }
        public void setYearOfStudy(Byte yearOfStudy) { this.yearOfStudy = yearOfStudy; }
        public String getStudentNumber() { return studentNumber; }
        public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }
    }

    // ── Nested: enrolled course summary ──────────────────────────────────
    public static class EnrolledCourseDTO {
        private String courseId;
        private String code;
        private String title;
        private String department;
        private int    totalSessions;    // completed sessions in this course
        private int    attendedSessions; // sessions student attended (present status)

        public String getCourseId() { return courseId; }
        public void setCourseId(String courseId) { this.courseId = courseId; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public int getTotalSessions() { return totalSessions; }
        public void setTotalSessions(int totalSessions) { this.totalSessions = totalSessions; }
        public int getAttendedSessions() { return attendedSessions; }
        public void setAttendedSessions(int attendedSessions) { this.attendedSessions = attendedSessions; }
    }

    // ── Nested: overall stats ─────────────────────────────────────────────
    public static class OverallStatsDTO {
        private double              avgConcentration;       // 0.0 – 1.0
        private double              avgAttentiveness;       // 0.0 – 1.0
        private long                totalLecturesAttended;
        private String              mostFrequentEmotion;    // EmotionType name
        private Map<String, Double> emotionBreakdown;       // e.g. {"happy": 0.35, …}

        public double getAvgConcentration() { return avgConcentration; }
        public void setAvgConcentration(double v) { this.avgConcentration = v; }
        public double getAvgAttentiveness() { return avgAttentiveness; }
        public void setAvgAttentiveness(double v) { this.avgAttentiveness = v; }
        public long getTotalLecturesAttended() { return totalLecturesAttended; }
        public void setTotalLecturesAttended(long v) { this.totalLecturesAttended = v; }
        public String getMostFrequentEmotion() { return mostFrequentEmotion; }
        public void setMostFrequentEmotion(String v) { this.mostFrequentEmotion = v; }
        public Map<String, Double> getEmotionBreakdown() { return emotionBreakdown; }
        public void setEmotionBreakdown(Map<String, Double> v) { this.emotionBreakdown = v; }
    }

    // ── Root getters & setters ────────────────────────────────────────────
    public StudentProfileDTO getStudentInfo() { return studentInfo; }
    public void setStudentInfo(StudentProfileDTO studentInfo) { this.studentInfo = studentInfo; }
    public List<EnrolledCourseDTO> getEnrolledCourses() { return enrolledCourses; }
    public void setEnrolledCourses(List<EnrolledCourseDTO> enrolledCourses) { this.enrolledCourses = enrolledCourses; }
    public List<LectureSummaryDTO> getRecentSummaries() { return recentSummaries; }
    public void setRecentSummaries(List<LectureSummaryDTO> recentSummaries) { this.recentSummaries = recentSummaries; }
    public OverallStatsDTO getOverallStats() { return overallStats; }
    public void setOverallStats(OverallStatsDTO overallStats) { this.overallStats = overallStats; }
    public List<RecommendationDTO> getRecommendations() { return recommendations; }
    public void setRecommendations(List<RecommendationDTO> recommendations) { this.recommendations = recommendations; }
}