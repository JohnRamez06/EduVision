package com.eduvision.strategy.authorization;

public interface AuthorizationStrategy {
    boolean hasPermission(String userId, String resource, String action);
}