package com.eduvision.repository;

import com.eduvision.model.PrivacyPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrivacyPolicyRepository extends JpaRepository<PrivacyPolicy, String> {
Optional<PrivacyPolicy> findByActiveTrue();
}