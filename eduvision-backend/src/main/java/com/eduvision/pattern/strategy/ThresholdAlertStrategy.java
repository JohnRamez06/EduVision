package com.eduvision.pattern.strategy;
import org.springframework.stereotype.Component;
@Component
public class ThresholdAlertStrategy implements AlertStrategy {
    @Override
    public boolean shouldAlert(double engagementScore, double concentration) {
        return engagementScore < 0.4 || concentration < 0.3;
    }
}
