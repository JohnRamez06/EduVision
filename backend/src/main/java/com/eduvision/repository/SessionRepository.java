package com.eduvision.repository;

import com.eduvision.model.LectureSession;
import com.eduvision.model.LectureSessionStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<LectureSession, String> {
    List<LectureSession> findByLecturerIdOrderByScheduledStartDesc(String lecturerId);

    List<LectureSession> findByCourseIdOrderByScheduledStartDesc(String courseId);

    Optional<LectureSession> findTopByCourseIdAndStatusOrderByScheduledStartDesc(String courseId, LectureSessionStatus status);
}
