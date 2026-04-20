package com.eduvision.strategy.authorization;
import org.springframework.stereotype.Component;
@Component
public class PermissionEvaluator {
    private AuthorizationStrategy strategy;
    public void setStrategy(AuthorizationStrategy strategy) { this.strategy = strategy; }
    public boolean check(String userId, String resource, String action) { return strategy.isAuthorized(userId, resource, action); }
}
