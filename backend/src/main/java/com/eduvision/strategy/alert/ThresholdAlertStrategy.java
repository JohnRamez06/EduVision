// src/main/java/com/eduvision/strategy/alert/ThresholdAlertStrategy.java
package com.eduvision.strategy.alert;

import com.eduvision.dto.alert.AlertContext;

public class ThresholdAlertStrategy implements AlertStrategy {
    
    private double confusionThreshold = 0.3;  // 30%
    private double engagementThreshold = 0.4; // 40%
    
    @Override
    public boolean shouldTrigger(AlertContext context) {
        return context.getConfusionRatio() > confusionThreshold ||
               context.getEngagementScore() < engagementThreshold;
    }
    
    @Override
    public String getAlertMessage(AlertContext context) {
        if (context.getConfusionRatio() > confusionThreshold) {
            return String.format("⚠️ High confusion detected: %.0f%% of students appear confused",
                context.getConfusionRatio() * 100);
        } else {
            return String.format("⚠️ Low engagement detected: %.0f%% engagement rate",
                context.getEngagementScore() * 100);
        }
    }
    
    @Override
    public String getAlertSeverity(AlertContext context) {
        if (context.getConfusionRatio() > confusionThreshold) {
            return "critical";
        } else {
            return "warning";
        }
    }
}