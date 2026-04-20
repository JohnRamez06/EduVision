package com.eduvision.strategy.authorization;
public interface AuthorizationStrategy { boolean isAuthorized(String userId, String resource, String action); }
