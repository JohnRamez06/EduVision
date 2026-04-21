package com.eduvision.repository;

import com.eduvision.model.StudentLectureSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentLectureSummaryRepository
        extends JpaRepository<StudentLectureSummary, String> {

    // Used in: getLectureSummaryBySession(), assignRole()
    Optional<StudentLectureSummary> findByStudent_IdAndSession_Id(
            String studentId, String sessionId);

    // Used in: getLectureSummaries(), getOverallStats(), generateRecommendations()
    List<StudentLectureSummary> findByStudent_IdOrderByGeneratedAtDesc(String studentId);

    // Used in: course-scoped timeline queries
    List<StudentLectureSummary> findByStudent_IdAndCourse_IdOrderByGeneratedAtDesc(
            String studentId, String courseId);

    // Used in: OverallStatsDTO.avgConcentration fast read
    @Query("SELECT AVG(s.avgConcentration) FROM StudentLectureSummary s " +
           "WHERE s.student.id = :studentId")
    Optional<Double> findAvgConcentrationByStudentId(@Param("studentId") String studentId);

    long countByStudent_Id(String studentId);
}