package com.eduvision.repository;

import com.eduvision.model.AttendanceStatus;
import com.eduvision.model.SessionAttendance;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionAttendanceRepository extends JpaRepository<SessionAttendance, String> {
    int countByStudent_IdAndSession_Course_IdAndStatus(String studentId, String courseId, AttendanceStatus status);

    int countByStudent_IdAndStatusIn(String studentId, Collection<AttendanceStatus> statuses);

    Optional<SessionAttendance> findByStudent_IdAndSession_Id(String studentId, String sessionId);

    List<SessionAttendance> findBySessionId(String sessionId);

    int countBySessionIdAndStatus(String sessionId, AttendanceStatus status);
}
