package com.eduvision.repository;

import com.eduvision.model.StudentEmotionSnapshot;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentEmotionSnapshotRepository extends JpaRepository<StudentEmotionSnapshot, String> {

    List<StudentEmotionSnapshot> findBySnapshotIdOrderByCapturedAtAsc(String snapshotId);

    List<StudentEmotionSnapshot> findBySessionIdOrderByCapturedAtAsc(String sessionId);

    List<StudentEmotionSnapshot> findByStudent_IdAndSession_IdOrderByCapturedAtAsc(String studentId, String sessionId);
}
