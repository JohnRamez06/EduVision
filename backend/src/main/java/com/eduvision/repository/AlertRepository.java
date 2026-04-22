package com.eduvision.repository;

import com.eduvision.model.Alert;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertRepository extends JpaRepository<Alert, String> {

    List<Alert> findBySessionIdOrderByTriggeredAtDesc(String sessionId);
}
