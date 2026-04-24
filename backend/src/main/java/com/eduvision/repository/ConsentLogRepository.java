package com.eduvision.repository;

import com.eduvision.model.ConsentLog;
import com.eduvision.model.ConsentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsentLogRepository extends JpaRepository<ConsentLog, String> {
    List<ConsentLog> findByStudent_IdAndStatus(String studentId, ConsentStatus status);
    List<ConsentLog> findByStudent_Id(String studentId);
}