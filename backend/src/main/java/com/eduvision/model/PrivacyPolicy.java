package com.eduvision.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "privacy_policies")
public class PrivacyPolicy {

    @Id
    @Column(name = "id", columnDefinition = "char(36)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "strategy_id")
    private Strategy strategy;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "version", nullable = false, length = 20)
    private String version;

    @Column(name = "store_face_data", nullable = false)
    private boolean storeFaceData;

    @Column(name = "store_frame_urls", nullable = false)
    private boolean storeFrameUrls;

    @Column(name = "anonymise_after_days", nullable = false)
    private int anonymiseAfterDays;

    @Column(name = "retention_days", nullable = false)
    private int retentionDays;

    @Column(name = "policy_text")
    private String policyText;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "policy")
    private Set<ConsentLog> consentLogs = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isStoreFaceData() {
        return storeFaceData;
    }

    public void setStoreFaceData(boolean storeFaceData) {
        this.storeFaceData = storeFaceData;
    }

    public boolean isStoreFrameUrls() {
        return storeFrameUrls;
    }

    public void setStoreFrameUrls(boolean storeFrameUrls) {
        this.storeFrameUrls = storeFrameUrls;
    }

    public int getAnonymiseAfterDays() {
        return anonymiseAfterDays;
    }

    public void setAnonymiseAfterDays(int anonymiseAfterDays) {
        this.anonymiseAfterDays = anonymiseAfterDays;
    }

    public int getRetentionDays() {
        return retentionDays;
    }

    public void setRetentionDays(int retentionDays) {
        this.retentionDays = retentionDays;
    }

    public String getPolicyText() {
        return policyText;
    }

    public void setPolicyText(String policyText) {
        this.policyText = policyText;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDateTime effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Set<ConsentLog> getConsentLogs() {
        return consentLogs;
    }

    public void setConsentLogs(Set<ConsentLog> consentLogs) {
        this.consentLogs = consentLogs;
    }
}
