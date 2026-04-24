package com.eduvision.repository;

import com.eduvision.model.CourseLecturer;
import com.eduvision.model.CourseLecturerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseLecturerRepository extends JpaRepository<CourseLecturer, CourseLecturerId> {
    
    // ✅ Correct: lecturer is a User object, User has 'id' field
    List<CourseLecturer> findByLecturerId(String userId);
    
    // Find by course ID
    @Query("SELECT cl FROM CourseLecturer cl WHERE cl.course.id = :courseId")
    List<CourseLecturer> findByCourseId(@Param("courseId") String courseId);
    
    // Find primary lecturer for a course
    @Query("SELECT cl FROM CourseLecturer cl WHERE cl.course.id = :courseId AND cl.primary = true")
    List<CourseLecturer> findByCourseIdAndPrimaryTrue(@Param("courseId") String courseId);
    
    // Find all courses for a lecturer
    @Query("SELECT cl FROM CourseLecturer cl WHERE cl.lecturer.id = :userId")
    List<CourseLecturer> findCoursesByLecturerId(@Param("userId") String userId);
}