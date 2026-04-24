package com.eduvision.service;

import com.eduvision.dto.consent.ConsentStatusDTO;
import com.eduvision.model.ConsentLog;
import com.eduvision.model.ConsentStatus;
import com.eduvision.model.PrivacyPolicy;
import com.eduvision.model.User;
import com.eduvision.repository.ConsentLogRepository;
import com.eduvision.repository.PrivacyPolicyRepository;
import com.eduvision.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ConsentService {

    @Autowired
    private ConsentLogRepository consentLogRepository;

    @Autowired
    private PrivacyPolicyRepository privacyPolicyRepository;

    @Autowired
    private UserRepository userRepository;

    public void grantConsent(String studentId, String policyId, String ipAddress) {
        Optional<User> student = userRepository.findById(studentId);
        Optional<PrivacyPolicy> policy = privacyPolicyRepository.findById(policyId);

        if (student.isPresent() && policy.isPresent()) {
            ConsentLog log = new ConsentLog();
            log.setStudent(student.get());
            log.setPolicy(policy.get());
            log.setStatus(ConsentStatus.granted);
            log.setConsentedAt(LocalDateTime.now());
            log.setIpAddress(ipAddress);
            log.setCreatedAt(LocalDateTime.now());
            consentLogRepository.save(log);
        }
    }

    public void revokeConsent(String studentId, String policyId) {
        List<ConsentLog> logs = consentLogRepository.findByStudent_IdAndStatus(studentId, ConsentStatus.granted);
        for (ConsentLog log : logs) {
            if (log.getPolicy().getId().equals(policyId)) {
                log.setStatus(ConsentStatus.revoked);
                log.setRevokedAt(LocalDateTime.now());
                consentLogRepository.save(log);
            }
        }
    }

    public boolean checkConsent(String studentId, String policyId) {
        List<ConsentLog> logs = consentLogRepository.findByStudent_IdAndStatus(studentId, ConsentStatus.granted);
        return logs.stream().anyMatch(log -> log.getPolicy().getId().equals(policyId));
    }

    public ConsentStatusDTO getConsentStatus(String studentId) {
        List<ConsentLog> logs = consentLogRepository.findByStudent_Id(studentId);
        Optional<ConsentLog> latest = logs.stream()
            .filter(log -> log.getStatus() == ConsentStatus.granted)
            .findFirst();

        ConsentStatusDTO dto = new ConsentStatusDTO();
        dto.setStudentId(studentId);
        dto.setHasConsented(latest.isPresent());
        if (latest.isPresent()) {
            dto.setConsentedAt(latest.get().getConsentedAt());
            dto.setPolicyVersion(latest.get().getPolicy().getVersion());
        }
        return dto;
    }
}