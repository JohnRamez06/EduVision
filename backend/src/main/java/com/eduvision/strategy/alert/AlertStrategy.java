package com.eduvision.strategy.alert;

import com.eduvision.dto.alert.AlertContext;
import com.eduvision.model.Alert;
import com.eduvision.model.AlertSeverity;
import com.eduvision.model.AlertStatus;
import org.springframework.stereotype.Component;

import java.util.List;

public interface AlertStrategy {

    boolean shouldTrigger(AlertContext context);

    Alert createAlert(AlertContext context);

    boolean shouldAlert(double engagementScore, double concentration);
}