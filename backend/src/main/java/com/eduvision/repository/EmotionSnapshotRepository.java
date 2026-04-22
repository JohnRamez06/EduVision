package com.eduvision.repository;

import com.eduvision.model.EmotionSnapshot;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmotionSnapshotRepository extends JpaRepository<EmotionSnapshot, String> {
    List<EmotionSnapshot> findBySessionIdOrderByCapturedAtAsc(String sessionId);

    Optional<EmotionSnapshot> findTopBySessionIdOrderByCapturedAtDesc(String sessionId);

    Optional<EmotionSnapshot> findByIdAndSessionId(String id, String sessionId);
}
