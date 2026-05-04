// src/main/java/com/eduvision/strategy/alert/CompositeAlertStrategy.java
package com.eduvision.strategy.alert;

import com.eduvision.dto.alert.AlertContext;
import java.util.ArrayList;
import java.util.List;

public class CompositeAlertStrategy implements AlertStrategy {
    
    private List<AlertStrategy> strategies = new ArrayList<>();
    
    public void addStrategy(AlertStrategy strategy) {
        strategies.add(strategy);
    }
    
    @Override
    public boolean shouldTrigger(AlertContext context) {
        return strategies.stream().anyMatch(s -> s.shouldTrigger(context));
    }
    
    @Override
    public String getAlertMessage(AlertContext context) {
        for (AlertStrategy strategy : strategies) {
            if (strategy.shouldTrigger(context)) {
                return strategy.getAlertMessage(context);
            }
        }
        return "Alert triggered";
    }
    
    @Override
    public String getAlertSeverity(AlertContext context) {
        for (AlertStrategy strategy : strategies) {
            if (strategy.shouldTrigger(context)) {
                return strategy.getAlertSeverity(context);
            }
        }
        return "info";
    }
}