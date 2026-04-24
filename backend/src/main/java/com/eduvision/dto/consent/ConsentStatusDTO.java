package com.eduvision.dto.consent;

import java.time.LocalDateTime;

public class ConsentStatusDTO {
    private String studentId;
    private boolean hasConsented;
    private LocalDateTime consentedAt;
    private String policyVersion;

    // Constructors, getters, setters
    public ConsentStatusDTO() {}

    public ConsentStatusDTO(String studentId, boolean hasConsented, LocalDateTime consentedAt, String policyVersion) {
        this.studentId = studentId;
        this.hasConsented = hasConsented;
        this.consentedAt = consentedAt;
        this.policyVersion = policyVersion;
    }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public boolean isHasConsented() { return hasConsented; }
    public void setHasConsented(boolean hasConsented) { this.hasConsented = hasConsented; }

    public LocalDateTime getConsentedAt() { return consentedAt; }
    public void setConsentedAt(LocalDateTime consentedAt) { this.consentedAt = consentedAt; }

    public String getPolicyVersion() { return policyVersion; }
    public void setPolicyVersion(String policyVersion) { this.policyVersion = policyVersion; }
}