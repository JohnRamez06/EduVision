package com.eduvision.strategy.authorization;

import org.springframework.stereotype.Component;

@Component
public class PermissionEvaluator {

    private AuthorizationStrategy strategy;

    // Setter Injection
    public void setStrategy(AuthorizationStrategy strategy) {
        this.strategy = strategy;
    }

    // Permission Check Method
    public boolean check(String userId, String resource, String action) {
        if (strategy == null) {
            throw new IllegalStateException("Authorization strategy is not set.");
        }

        return strategy.hasPermission(userId, resource, action);
    }
}