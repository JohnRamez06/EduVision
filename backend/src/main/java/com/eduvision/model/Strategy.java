package com.eduvision.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "strategies")
public class Strategy {

    @Id
    @Column(name = "id", columnDefinition = "char(36)")
    private String id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private StrategyType type;

    @Column(name = "handler_class", nullable = false)
    private String handlerClass;

    @Column(name = "config", columnDefinition = "json")
    private String config;

    @Column(name = "is_default", nullable = false)
    private boolean defaultStrategy;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "strategy")
    private Set<Alert> alerts = new HashSet<>();

    @OneToMany(mappedBy = "strategy")
    private Set<PrivacyPolicy> privacyPolicies = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public StrategyType getType() {
        return type;
    }

    public void setType(StrategyType type) {
        this.type = type;
    }

    public String getHandlerClass() {
        return handlerClass;
    }

    public void setHandlerClass(String handlerClass) {
        this.handlerClass = handlerClass;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public boolean isDefaultStrategy() {
        return defaultStrategy;
    }

    public void setDefaultStrategy(boolean defaultStrategy) {
        this.defaultStrategy = defaultStrategy;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<Alert> getAlerts() {
        return alerts;
    }

    public void setAlerts(Set<Alert> alerts) {
        this.alerts = alerts;
    }

    public Set<PrivacyPolicy> getPrivacyPolicies() {
        return privacyPolicies;
    }

    public void setPrivacyPolicies(Set<PrivacyPolicy> privacyPolicies) {
        this.privacyPolicies = privacyPolicies;
    }
}
