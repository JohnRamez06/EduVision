package com.eduvision.strategy.privacy;
import org.springframework.stereotype.Component;
@Component("GDPRPrivacyStrategy")
public class GDPRPrivacyStrategy implements PrivacyStrategy {
    @Override
    public Object apply(Object data, String studentId) {
        return data;
    }
}