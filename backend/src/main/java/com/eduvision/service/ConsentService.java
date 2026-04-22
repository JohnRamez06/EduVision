package com.eduvision.service;

import com.eduvision.exception.ResourceNotFoundException;
import com.eduvision.model.ConsentLog;
import com.eduvision.model.ConsentStatus;
import com.eduvision.model.PrivacyPolicy;
import com.eduvision.model.User;
import com.eduvision.repository.ConsentRepository;
import com.eduvision.repository.PrivacyPolicyRepository;
import com.eduvision.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsentService {

    private final ConsentRepository consentRepository;
    private final UserRepository userRepository;
    private final PrivacyPolicyRepository privacyPolicyRepository;

    public ConsentService(ConsentRepository consentRepository,
                          UserRepository userRepository,
                          PrivacyPolicyRepository privacyPolicyRepository) {
        this.consentRepository = consentRepository;
        this.userRepository = userRepository;
        this.privacyPolicyRepository = privacyPolicyRepository;
    }

    @Transactional
    public void grantConsent(String studentId, String policyId) {
        persistConsent(studentId, policyId, ConsentStatus.granted);
    }

    @Transactional
    public void revokeConsent(String studentId, String policyId) {
        persistConsent(studentId, policyId, ConsentStatus.revoked);
    }

    private void persistConsent(String studentId, String policyId, ConsentStatus status) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + studentId));
        PrivacyPolicy policy = privacyPolicyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Privacy policy not found: " + policyId));

        ConsentLog log = new ConsentLog();
        log.setId(UUID.randomUUID().toString());
        log.setStudent(student);
        log.setPolicy(policy);
        log.setStatus(status);
        log.setCreatedAt(LocalDateTime.now());
        if (status == ConsentStatus.granted) {
            log.setConsentedAt(LocalDateTime.now());
        } else if (status == ConsentStatus.revoked) {
            log.setRevokedAt(LocalDateTime.now());
        }
        consentRepository.save(log);
    }
}
