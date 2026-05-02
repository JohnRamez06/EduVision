package com.eduvision.repository;

import com.eduvision.model.StudentEmotionSnapshot;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentEmotionSnapshotRepository extends JpaRepository<StudentEmotionSnapshot, String> {
    List<StudentEmotionSnapshot> findBySnapshotId(String snapshotId);

    List<StudentEmotionSnapshot> findBySessionIdOrderByCapturedAtAsc(String sessionId);

    List<StudentEmotionSnapshot> findBySessionIdAndStudentIdOrderByCapturedAtAsc(String sessionId, String studentId);

    List<StudentEmotionSnapshot> findByStudent_IdAndSession_IdOrderByCapturedAtAsc(String studentId, String sessionId);

    List<StudentEmotionSnapshot> findBySession_IdOrderByCapturedAtDesc(String sessionId);
}
