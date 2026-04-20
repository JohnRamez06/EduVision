package com.eduvision.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CourseStudentId implements Serializable {

    @Column(name = "course_id", columnDefinition = "char(36)")
    private String courseId;

    @Column(name = "student_id", columnDefinition = "char(36)")
    private String studentId;

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CourseStudentId that)) {
            return false;
        }
        return Objects.equals(courseId, that.courseId)
                && Objects.equals(studentId, that.studentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId, studentId);
    }
}
