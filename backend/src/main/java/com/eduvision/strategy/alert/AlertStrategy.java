// src/main/java/com/eduvision/strategy/alert/AlertStrategy.java
package com.eduvision.strategy.alert;

import com.eduvision.dto.alert.AlertContext;

public interface AlertStrategy {
    boolean shouldTrigger(AlertContext context);
    String getAlertMessage(AlertContext context);
    String getAlertSeverity(AlertContext context);
}