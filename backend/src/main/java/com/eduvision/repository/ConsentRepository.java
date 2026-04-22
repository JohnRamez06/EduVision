package com.eduvision.repository;

import com.eduvision.model.ConsentLog;
import com.eduvision.model.PrivacyPolicy;
import com.eduvision.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsentRepository extends JpaRepository<ConsentLog, String> {
    Optional<ConsentLog> findTopByStudentAndPolicyOrderByCreatedAtDesc(User student, PrivacyPolicy policy);
}
