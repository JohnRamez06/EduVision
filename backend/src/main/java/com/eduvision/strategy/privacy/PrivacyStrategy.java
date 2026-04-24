package com.eduvision.strategy.privacy;

public interface PrivacyStrategy {
    Object filterStudentData(Object data, String studentId);
    boolean shouldAnonymize(String studentId, long dataAgeInDays);
}