package com.eduvision.strategy.alert;

import com.eduvision.dto.alert.AlertContext;
import com.eduvision.model.Alert;
import com.eduvision.model.AlertSeverity;
import com.eduvision.model.AlertStatus;
import org.springframework.stereotype.Component;

@Component("TrendAlertStrategy")
public class TrendAlertStrategy implements AlertStrategy {

    private static final double TREND_THRESHOLD = -0.1;

    @Override
    public boolean shouldTrigger(AlertContext context) {
        return context.getTrendSlope() < TREND_THRESHOLD ||
               (context.getPreviousEngagementScore() > 0 &&
                context.getEngagementScore() < context.getPreviousEngagementScore());
    }

    @Override
    public boolean shouldAlert(double engagementScore, double concentration) {
        // Trend strategy depends on full context, so fallback basic check
        return false;
    }

    @Override
    public Alert createAlert(AlertContext context) {
        Alert alert = new Alert();
        alert.setSeverity(AlertSeverity.LOW);
        alert.setStatus(AlertStatus.ACTIVE);
        alert.setTitle("Engagement Trend Alert");
        alert.setMessage("Student engagement is trending downward");

        return alert;
    }
}