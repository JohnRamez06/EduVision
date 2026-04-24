package com.eduvision.strategy.privacy;

import org.springframework.stereotype.Component;

@Component("GDPRPrivacyStrategy")
public class GDPRPrivacyStrategy implements PrivacyStrategy {

    private static final int GDPR_RETENTION_DAYS = 2555; // ~7 years

    @Override
    public Object filterStudentData(Object data, String studentId) {
        // Implement GDPR-compliant data filtering
        // Remove or anonymize personal data
        if (data instanceof String) {
            // For example, anonymize names, emails, etc.
            return anonymizeString((String) data);
        }
        return data;
    }

    @Override
    public boolean shouldAnonymize(String studentId, long dataAgeInDays) {
        // GDPR requires data to be deleted after retention period
        return dataAgeInDays > GDPR_RETENTION_DAYS;
    }

    private String anonymizeString(String data) {
        // Simple anonymization: replace with hashes or remove sensitive info
        // In real implementation, use proper anonymization techniques
        return "ANONYMIZED";
    }
}