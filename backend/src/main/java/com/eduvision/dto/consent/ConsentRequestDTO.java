package com.eduvision.dto.consent;

public class ConsentRequestDTO {
    private String policyId;
    private String status; // GRANT or REVOKE

    // Constructors, getters, setters
    public ConsentRequestDTO() {}

    public ConsentRequestDTO(String policyId, String status) {
        this.policyId = policyId;
        this.status = status;
    }

    public String getPolicyId() { return policyId; }
    public void setPolicyId(String policyId) { this.policyId = policyId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}