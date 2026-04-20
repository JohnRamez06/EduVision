package com.eduvision.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @Column(name = "user_id", columnDefinition = "char(36)")
    private String userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "student_number", nullable = false, unique = true, length = 60)
    private String studentNumber;

    @Column(name = "program", length = 200)
    private String program;

    @Column(name = "year_of_study")
    private Byte yearOfStudy;

    @Column(name = "gpa", precision = 3, scale = 2)
    private BigDecimal gpa;

    @Column(name = "enrolled_at")
    private LocalDate enrolledAt;

    @Column(name = "expected_grad")
    private LocalDate expectedGrad;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public Byte getYearOfStudy() {
        return yearOfStudy;
    }

    public void setYearOfStudy(Byte yearOfStudy) {
        this.yearOfStudy = yearOfStudy;
    }

    public BigDecimal getGpa() {
        return gpa;
    }

    public void setGpa(BigDecimal gpa) {
        this.gpa = gpa;
    }

    public LocalDate getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(LocalDate enrolledAt) {
        this.enrolledAt = enrolledAt;
    }

    public LocalDate getExpectedGrad() {
        return expectedGrad;
    }

    public void setExpectedGrad(LocalDate expectedGrad) {
        this.expectedGrad = expectedGrad;
    }
}
