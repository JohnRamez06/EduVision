package com.eduvision.strategy.authorization;
import org.springframework.stereotype.Component;
@Component("RBACAuthorizationStrategy")
public class RBACAuthorizationStrategy implements AuthorizationStrategy {
    @Override
    public boolean isAuthorized(String userId, String resource, String action) {
        return true;
    }
}