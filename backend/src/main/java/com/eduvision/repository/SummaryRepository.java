package com.eduvision.repository;

import com.eduvision.model.StudentLectureSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SummaryRepository extends JpaRepository<StudentLectureSummary, String> {

    // Latest summaries for a student (most recent first)
    List<StudentLectureSummary> findByStudent_IdOrderByGeneratedAtDesc(String studentId);

    // Summary for a specific student + session
    Optional<StudentLectureSummary> findByStudent_IdAndSession_Id(String studentId, String sessionId);

    // Course-scoped timeline
    List<StudentLectureSummary> findByStudent_IdAndCourse_IdOrderByGeneratedAtDesc(String studentId, String courseId);

    // Lecturer dashboard: all summaries in a session (for at-risk list, etc.)
    List<StudentLectureSummary> findBySession_Id(String sessionId);

    // Useful quick aggregate
    @Query("SELECT AVG(s.avgConcentration) FROM StudentLectureSummary s WHERE s.student.id = :studentId")
    Optional<Double> findAvgConcentrationByStudentId(@Param("studentId") String studentId);

    long countByStudent_Id(String studentId);
}