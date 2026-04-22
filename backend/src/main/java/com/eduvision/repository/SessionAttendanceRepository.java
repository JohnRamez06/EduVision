package com.eduvision.repository;

import com.eduvision.model.AttendanceStatus;
import com.eduvision.model.SessionAttendance;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionAttendanceRepository extends JpaRepository<SessionAttendance, String> {

    Integer countByStudent_IdAndSession_Course_IdAndStatus(String studentId, String courseId, AttendanceStatus status);

    Optional<SessionAttendance> findByStudent_IdAndSession_Id(String studentId, String sessionId);
}
