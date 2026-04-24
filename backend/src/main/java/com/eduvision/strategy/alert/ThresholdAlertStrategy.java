package com.eduvision.strategy.alert;

import com.eduvision.dto.alert.AlertContext;
import com.eduvision.model.Alert;
import com.eduvision.model.AlertSeverity;
import com.eduvision.model.AlertStatus;
import org.springframework.stereotype.Component;

@Component("ThresholdAlertStrategy")
public class ThresholdAlertStrategy implements AlertStrategy {

    private static final double ENGAGEMENT_THRESHOLD = 0.3;
    private static final double CONCENTRATION_THRESHOLD = 0.4;

    @Override
    public boolean shouldTrigger(AlertContext context) {
        return context.getEngagementScore() < ENGAGEMENT_THRESHOLD ||
               context.getConcentrationLevel() < CONCENTRATION_THRESHOLD;
    }

    @Override
    public boolean shouldAlert(double engagementScore, double concentration) {
        return engagementScore < ENGAGEMENT_THRESHOLD ||
               concentration < CONCENTRATION_THRESHOLD;
    }

    @Override
    public Alert createAlert(AlertContext context) {
        Alert alert = new Alert();
        alert.setSeverity(AlertSeverity.MEDIUM);
        alert.setStatus(AlertStatus.ACTIVE);
        alert.setTitle("Low Engagement Alert");
        alert.setMessage("Student engagement or concentration below threshold");

        return alert;
    }
}