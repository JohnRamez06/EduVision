// src/main/java/com/eduvision/strategy/alert/TrendAlertStrategy.java
package com.eduvision.strategy.alert;

import com.eduvision.dto.alert.AlertContext;

public class TrendAlertStrategy implements AlertStrategy {
    
    private double trendThreshold = 0.2; // 20% drop
    
    // This would normally look at historical data
    private double previousEngagementScore = 0.75; // Example value
    
    @Override
    public boolean shouldTrigger(AlertContext context) {
        double drop = previousEngagementScore - context.getEngagementScore();
        return drop > trendThreshold;
    }
    
    @Override
    public String getAlertMessage(AlertContext context) {
        double drop = previousEngagementScore - context.getEngagementScore();
        return String.format("📉 Engagement dropped by %.0f%% compared to previous session",
            drop * 100);
    }
    
    @Override
    public String getAlertSeverity(AlertContext context) {
        double drop = previousEngagementScore - context.getEngagementScore();
        if (drop > 0.3) return "critical";
        if (drop > 0.2) return "warning";
        return "info";
    }
}