package com.eduvision.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @Column(name = "id", columnDefinition = "char(36)")
    private String id;

    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "credit_hours", nullable = false)
    private byte creditHours;

    @Column(name = "department", length = 150)
    private String department;

    @Column(name = "semester", length = 30)
    private String semester;

    @Column(name = "academic_year", length = 10)
    private String academicYear;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "course")
    private Set<CourseLecturer> courseLecturers = new HashSet<>();

    @OneToMany(mappedBy = "course")
    private Set<CourseStudent> courseStudents = new HashSet<>();

    @OneToMany(mappedBy = "course")
    private Set<LectureSession> lectureSessions = new HashSet<>();

    @OneToOne(mappedBy = "course")
    private LectureSessionRegistry lectureSessionRegistry;

    @OneToMany(mappedBy = "course")
    private Set<StudentLectureSummary> studentLectureSummaries = new HashSet<>();

    @OneToMany(mappedBy = "course")
    private Set<Alert> alerts = new HashSet<>();

    @OneToMany(mappedBy = "course")
    private Set<Report> reports = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte getCreditHours() {
        return creditHours;
    }

    public void setCreditHours(byte creditHours) {
        this.creditHours = creditHours;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
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

    public Set<CourseLecturer> getCourseLecturers() {
        return courseLecturers;
    }

    public void setCourseLecturers(Set<CourseLecturer> courseLecturers) {
        this.courseLecturers = courseLecturers;
    }

    public Set<CourseStudent> getCourseStudents() {
        return courseStudents;
    }

    public void setCourseStudents(Set<CourseStudent> courseStudents) {
        this.courseStudents = courseStudents;
    }

    public Set<LectureSession> getLectureSessions() {
        return lectureSessions;
    }

    public void setLectureSessions(Set<LectureSession> lectureSessions) {
        this.lectureSessions = lectureSessions;
    }

    public LectureSessionRegistry getLectureSessionRegistry() {
        return lectureSessionRegistry;
    }

    public void setLectureSessionRegistry(LectureSessionRegistry lectureSessionRegistry) {
        this.lectureSessionRegistry = lectureSessionRegistry;
    }

    public Set<StudentLectureSummary> getStudentLectureSummaries() {
        return studentLectureSummaries;
    }

    public void setStudentLectureSummaries(Set<StudentLectureSummary> studentLectureSummaries) {
        this.studentLectureSummaries = studentLectureSummaries;
    }

    public Set<Alert> getAlerts() {
        return alerts;
    }

    public void setAlerts(Set<Alert> alerts) {
        this.alerts = alerts;
    }

    public Set<Report> getReports() {
        return reports;
    }

    public void setReports(Set<Report> reports) {
        this.reports = reports;
    }
}
