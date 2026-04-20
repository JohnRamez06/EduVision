package com.eduvision.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CourseLecturerId implements Serializable {

    @Column(name = "course_id", columnDefinition = "char(36)")
    private String courseId;

    @Column(name = "lecturer_id", columnDefinition = "char(36)")
    private String lecturerId;

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(String lecturerId) {
        this.lecturerId = lecturerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CourseLecturerId that)) {
            return false;
        }
        return Objects.equals(courseId, that.courseId)
                && Objects.equals(lecturerId, that.lecturerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId, lecturerId);
    }
}
