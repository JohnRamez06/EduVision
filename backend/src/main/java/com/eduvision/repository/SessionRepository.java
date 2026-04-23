package com.eduvision.repository;

import com.eduvision.model.LectureSession;
import com.eduvision.model.LectureSessionStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<LectureSession, String> {
    List<LectureSession> findByLecturerIdOrderByScheduledStartDesc(String lecturerId);

    List<LectureSession> findByCourseIdOrderByScheduledStartDesc(String courseId);

    Optional<LectureSession> findTopByCourseIdAndStatusOrderByScheduledStartDesc(String courseId, LectureSessionStatus status);

    List<LectureSession> findByCourseIdAndStatus(String courseId, LectureSessionStatus status);

    @Query("SELECT s FROM LectureSession s WHERE s.course.id = :courseId AND s.status = com.eduvision.model.LectureSessionStatus.active")
    Optional<LectureSession> findActiveSessionByCourseId(@Param("courseId") String courseId);

    List<LectureSession> findByStatus(LectureSessionStatus status);
}
