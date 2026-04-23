package com.eduvision.repository;

import com.eduvision.model.Alert;
import com.eduvision.model.AlertStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertRepository extends JpaRepository<Alert, String> {
    List<Alert> findBySessionIdOrderByTriggeredAtDesc(String sessionId);
    List<Alert> findBySession_Id(String sessionId);
    List<Alert> findBySession_IdAndStatus(String sessionId, AlertStatus status);
    List<Alert> findByCourse_Id(String courseId);
    List<Alert> findByStatus(AlertStatus status);
}

